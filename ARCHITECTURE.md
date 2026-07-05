# Clarity — Foundation Architecture

Decision log for the Phase 1 foundation build. Every choice below is deliberate;
if a future phase wants to change one, this document says what it would be
trading away.

---

## 1. Layering — Clean Architecture, pragmatically applied

Three layers with one-way dependencies:

```
ui / feature  ─▶  domain  ◀─  data
```

- **domain** — pure Kotlin: models (`ThemeMode`) and repository *interfaces*
  (`SettingsRepository`). No Android imports, no framework types beyond
  `kotlinx.coroutines.Flow`. This is where Phase A's streak engine, recovery
  score, and encouragement logic will live — fully unit-testable on the JVM,
  no emulator.
- **data** — implementations: DataStore sources, Room entities/DAOs,
  repository impls. Depends on domain (implements its interfaces), never the
  reverse.
- **ui / feature** — Compose + ViewModels. Depends on domain interfaces only;
  no ViewModel ever imports a DAO or DataStore.

Single Gradle module for now. Multi-module (`:core:domain`, `:core:data`, …)
adds real build-speed value only past ~30–40k LOC or with multiple contributors;
the package discipline above keeps the seams clean so a later split is
mechanical, not a rewrite.

## 2. Package strategy — layers for plumbing, features for screens

`domain/` and `data/` are organized by layer (they're shared infrastructure);
screens live under `feature/<name>/` (onboarding today; home, journey, sos, …
in Phase A). Cross-cutting UI lives in `core/designsystem/`. This hybrid is
what Now-in-Android converged on: feature folders keep each screen's VM +
composables together; layer folders stop five features from each inventing
their own data access.

## 3. MVVM conventions

- **One `StateFlow<UiState>` per ViewModel**, modeled as a sealed interface
  (`Loading / Ready / …`). One observable object per screen means recomposition
  is predictable and state is trivially loggable/testable. No LiveData anywhere.
- Events flow **down** (lambdas), state flows **up** (StateFlow). No shared
  mutable state between screens; cross-screen facts live in repositories.
- No `BaseViewModel`. Inheritance hierarchies in VMs calcify fast; shared
  behavior arrives as extension functions or injected collaborators instead.
- `MainViewModel` uses `SharingStarted.Eagerly` — deliberate exception to the
  usual `WhileSubscribed(5000)`: the splash `keepOnScreenCondition` reads
  `uiState.value` *before* the first composition subscribes, so the flow must
  already be hot or the splash would never dismiss.

## 4. Dependency injection — Hilt

- `@HiltAndroidApp` on the Application, `@AndroidEntryPoint` on the single
  activity, `@HiltViewModel` everywhere else.
- Modules are split by concern: `DatabaseModule` (Room + SQLCipher wiring),
  `DataModule` (`@Binds` interface→impl). `@Binds` over `@Provides` where
  possible — zero generated wrapper code.
- Everything singleton-scoped for now; feature-scoped components only if a
  real lifecycle problem appears (they rarely do in single-activity apps).

## 5. Persistence split — Room vs DataStore

| Store | Holds | Why |
|---|---|---|
| **Room (SQLCipher-encrypted)** | user data: journey events, check-ins, journal… (Phase A+) | relational, queryable, *sensitive* |
| **Preferences DataStore** | app configuration: theme, onboarding flag, later notification prefs | tiny key/value, reactive, not secret |

The rule: *user data is encrypted; app configuration is not.* Encrypting the
theme preference would buy nothing and cost a Keystore round-trip on every
cold start before the first frame.

## 6. Database encryption — SQLCipher + Keystore-wrapped passphrase

- A 256-bit random passphrase is generated once, wrapped with an AES-GCM key
  living in **Android Keystore** (StrongBox when available, TEE otherwise),
  and only the wrapped blob is persisted (`DatabasePassphraseManager`).
- The raw passphrase never touches disk. The Keystore key can't leave the
  hardware.
- **Independent of the user's app-lock PIN** (Phase A): the PIN gates the UI;
  the passphrase gates the bytes. A PIN change never re-encrypts the database,
  and forgetting the PIN never destroys data.
- Wired as a Room `openHelperFactory` in DI — nothing below the DI layer knows
  encryption exists, so every DAO/repository test can run against plain
  in-memory Room.
- `fallbackToDestructiveMigration` is deliberately absent. This database will
  hold someone's entire recovery history; silent data loss is never an
  acceptable failure mode. Real migrations from v2 onward, enabled by
  `exportSchema = true` + committed schema JSONs in `/app/schemas`.

## 7. Room baseline — one entity, on purpose

`app_metadata` (singleton row: first-launch timestamp, seeded-content version)
is the only v1 entity. It anchors Phase A's content seeder (compare bundled
JSON version → upsert delta) and gives migrations a real v1 to migrate *from*.
Feature entities land with their features — shipping speculative schema now
would just mean migrating guesses later.

## 8. Navigation — type-safe, single activity, SOS as an action

- **Single activity**, all screens are Navigation Compose destinations.
- **Type-safe routes** (`@Serializable data object HomeRoute`) — route args in
  Phase A become constructor parameters, checked at compile time, no string
  templates.
- **SOS is not a tab.** It's a raised center button that *pushes* `SosRoute`
  on top of whatever tab you're in — a user reaching for help never loses
  their place, and back returns them exactly where they were. The bottom bar
  hides on non-top-level destinations so full-screen flows (SOS; later the
  relapse reset and celebrations) have no competing chrome.
- Tab switches use the standard save/restore pattern (`popUpTo(start) {
  saveState }` + `restoreState` + `launchSingleTop`): one instance per tab,
  scroll positions survive, back always lands on Home.
- The bar itself is custom (Surface + Row + selectable items), not
  `NavigationBar` — M3's component can't express a protruding center action
  without a disabled dummy item. Items use `Role.Tab` semantics and the
  whole component sits in one Box tall enough to keep the 64 dp button fully
  hit-testable.

## 9. Theme system

- **Palette**: deep teal (trust, calm) over near-black teal surfaces in dark
  mode (`#0D1414` — OLED-friendly); **dawn amber** reserved for celebration
  and the SOS button — the single loud element. **No alarm red anywhere**:
  even `error` is a muted clay. A recovery app must never punish visually.
- **Dynamic color intentionally not wired.** Material You would dissolve the
  brand into wallpaper colors; the palette *is* the identity. Revisit as an
  opt-in setting only if users ask.
- **ExtendedColors** (`success / warning / celebration`) via CompositionLocal
  for semantics M3 has no slot for; accessed as `MaterialTheme.extended.*`.
- **Spacing tokens** (`MaterialTheme.spacing.md` …) instead of dp literals —
  consistent rhythm is the difference between "minimal" and "sparse".
- **Shapes**: generous radii (8→28 dp), part of the soft identity.
- **Dark mode**: full parallel scheme, user-selectable
  (SYSTEM / LIGHT / DARK) persisted in DataStore, applied at the root.

## 10. Typography — serif display over grotesque body

Fraunces (display/headlines: streak numbers, quotes) over Inter (body/UI).
The warm serif is what keeps Clarity from reading like a clinical tracker.
Fonts resolve through the Google Fonts provider — served by Play services, so
the app still declares **zero permissions**; if unavailable, Compose falls
back to platform serif/sans. **Before Play Store submission**: bundle the two
TTFs under `res/font` and swap two vals in `Type.kt` for guaranteed identical
rendering (kept downloadable here to keep the repo binary-free).

## 11. Component inventory (core/designsystem/components)

- `ClarityPrimaryButton` (loading state without layout jump), `ClaritySecondaryButton`, `ClarityTextButton` — 52 dp min height, one primary per screen.
- `ClarityCard` — tonal elevation + hairline outline, no drop shadows (reads premium, survives dark mode).
- `BreathingIndicator` / `LoadingScreen` — the brand mark breathes at ~1.8 s per phase; loading quietly rehearses the app's core coping exercise.
- `ErrorScreen` — say what happened, say the data is safe, offer the fix. No red, no theatrical apology.
- `EmptyState` — an invitation to act; callers should almost always pass an action.
- `PlaceholderScreen` — every unbuilt destination renders through this, loudly labeled `FOUNDATION BUILD — PLACEHOLDER`, deleted feature-by-feature.

## 12. Splash & cold start

`androidx.core.splashscreen` with `keepOnScreenCondition` bound to
`MainUiState.Loading`: the system splash holds until theme + onboarding state
are read from DataStore (single-digit ms), so there is never a wrong-theme
flash or a start-screen swap. Post-splash XML theme only sets
`windowBackground` per mode; Compose owns everything else.

## 13. Accessibility baseline

- Reduce-motion respected from day one (`rememberReduceMotionEnabled`):
  decorative motion degrades to static. Phase B celebrations must gate on it.
- Touch targets ≥ 44–52 dp; nav items use `selectable(role = Tab)` +
  `selectableGroup` so TalkBack announces "tab, 2 of 4".
- Icon-only controls always carry `contentDescription` (SOS button); decorative
  icons pass `null` so labels aren't read twice.

## 14. Privacy posture (enforced by the manifest, not promised by copy)

- **Zero permissions** — no INTERNET at all in the foundation. Offline isn't a
  setting, it's structurally guaranteed.
- `allowBackup=false` + explicit empty `dataExtractionRules`: the encrypted DB
  and wrapped key never ride cloud backup or device transfer (Keystore keys
  can't move anyway; a transferred copy would be unreadable *and* leak
  metadata).
- Explicit, passphrase-encrypted export is the Phase C answer to backup.

## 15. Intentionally absent (and when it arrives)

- **Tests** — arrive with the first real domain logic (Phase A streak engine),
  where they pay for themselves. Testing the current pass-through repository
  would test the mocking library.
- **FLAG_SECURE / app lock** — ships with the settings screen that controls it
  (Phase A). Hardcoding it now would break the user's own screenshots with no
  way to opt out.
- **WorkManager / notifications** — Phase A, with the check-in reminder.
- **Baseline profiles, CI** — once there's a release candidate to profile.

## 16. Onboarding & the recovery plan (Phase A, step 1)

- **Answers are closed enums with stable `storageValue`s** (same contract as
  `ThemeMode`): refactors can't corrupt rows, and the generator switches over
  exhaustive types — a new answer option without handling is a compile error.
- **The plan is stored as codes, not prose.** `recovery_plan_item` rows hold
  `PlanItemCode` storage strings; the UI resolves each code to localized
  title/description resources. Copy improvements and translations ship with
  app updates and never require touching saved data.
- **`RecoveryPlanGenerator` is pure, deterministic, and unit-tested** — the
  first real domain logic, so the first tests (per §15). Same profile ->
  identical plan, every item traceable to a specific answer; a future "why is
  this in my plan?" screen is a lookup, not a guess.
- **First milestone scales with proven history** (never tried / <1wk -> 7
  days; 1–4wk -> 14; 1mo+ -> 30): an achievable first win beats an
  intimidating one.
- **DB v1 -> v2 is a hand-written migration** (`DatabaseMigrations.kt`),
  honoring the no-destructive-migration rule from day one. Verify the SQL
  against the exported `app/schemas/2.json` after first build.
- **Step navigation is ViewModel state, not NavController routes.**
  Onboarding is one experience with one entry and one exit; keeping its steps
  out of the graph means no destination exists that must never be reachable
  post-onboarding. Trade-off: an in-progress draft dies with process death —
  acceptable for a two-minute flow, documented here.
- **Explicit Continue, no auto-advance on selection.** Slower by one tap per
  step, but deliberate (this is reflection, not a quiz), mis-tap-proof, and
  predictable under TalkBack.
- **Adults-only age ranges (18+)** align the product's positioning with the
  mature store rating its subject matter already implies.
- **The generation pause is pacing, not computation** — generation is
  instantaneous; ~2.2s of staged status lines give the reveal weight. The
  code says so honestly.
- **Completion ordering:** persist profile+plan (one Room transaction), then
  flip the DataStore flag the shell observes. The shell swap *is* the success
  signal; a crash between the two leaves a completed profile and an
  incomplete flag — onboarding simply runs again, which is safe.

## 17. Streaks, check-ins, journal (Phase A, step 2)

- **The streak is derived, never stored** (AD-1 realized). `StreakCalculator`
  folds the recovery start day + append-only RELAPSE events into a snapshot;
  there is no counter anywhere that could drift from the truth. Semantics
  (unit-pinned): a clean run starts the day *after* a relapse (relapse today
  = 0, tomorrow = Day 1); the very first run starts *on* the recovery start
  day (onboarding evening = Day 1); today counts while clean; a closed run
  excludes its relapse day. "Today" is a parameter, never a clock read — the
  midnight bug class is untestable-flaky by construction, so it's designed
  out instead.
- **Check-ins do not gate the streak.** Days clean accrue by being lived,
  not by being logged; punishing a missed log would violate the no-shame
  rule. Check-ins are their own signal: one upserted row per local day
  (mood 1–5 required, urge 0–10), editable all day.
- **No relapse UI yet, but full relapse math now** — the compassionate reset
  flow later is "insert one event"; its semantics are already tested.
- **Timezone policy:** every dated row stores `epochDay` computed in the
  device zone at write time; reads never re-derive dates from millis. The
  Home frame captures "today" at ViewModel init — a session held open across
  midnight shows the old frame until re-entry (accepted v1 trade-off; writes
  are exempt, stamping their own day at save time).
- **Daily quote is pure math**: `epochDay % quotes.size` over an all-original
  strings-array — same line all day, changes at local midnight, zero storage,
  zero licensing exposure.
- **Journal is deliberately titleless** — a title prompt is friction, and
  friction kills the habit. Delete sits behind a confirm; back discards
  unsaved edits (v1 trade-off, kept honest by Save being the only
  persistence signal). The editor is a non-top-level destination, so the
  bottom bar auto-hides — the shell design paying off unchanged.
- **DB v2 -> v3** adds `daily_checkin`, `journal_entry`, `journey_event`
  via hand-written migration; `journey_event` has no update/delete DAO
  methods — append-only enforced at the API surface, not by convention.

## 18. Habit system & reminders (Phase B)

- **The permission line moved, deliberately.** POST_NOTIFICATIONS (runtime,
  13+) and RECEIVE_BOOT_COMPLETED enter the manifest for local reminders.
  The real privacy guarantee — NO INTERNET, ever — is untouched; nothing in
  this app can move data off the device. The manifest comment says exactly
  this.
- **Inexact alarms, chained.** `setAndAllowWhileIdle` one-shots that re-arm
  on fire. Not exact alarms (USE_EXACT_ALARM is Play-restricted to
  alarm-clock apps; SCHEDULE_EXACT_ALARM is user-revocable and default-denied
  on 14+) and not WorkManager (15-minute periodic drift is wrong for "9 PM").
  A habit nudge landing within Doze's batching window is exactly as useful.
  Next-trigger math is a pure, unit-tested function of (mask, minute, now).
- **Reminders are self-healing**: re-armed on boot (receiver), on app start
  (Application scope), and on every fire (chain). The fire path reads the
  habit fresh from the DB, so deleted/muted habits drop silently instead of
  resurrecting from stale intent extras.
- **Notification copy rule enforced in code**: title = the user's own habit
  name, body = neutral encouragement. Nothing clinical, nothing explicit.
- **Schedule = 7-bit ISO weekday mask.** Compact, exhaustively testable, and
  `Habit` refuses an empty mask (the editor allows a zero-mask *draft*; the
  disabled Save button is the explanation).
- **One aggregation, two consumers**: `HabitStatsCalculator` feeds both the
  week chart and `InsightGenerator`, so the graph and the words can never
  disagree. Habits created mid-window only count from their creation day —
  a habit made Thursday can't "miss" Monday.
- **Insights are typed codes with conservative thresholds** (same contract as
  plan items). An insight that fires on noise teaches users to ignore
  insights, so every rule needs a real sample size, rules run in priority
  order, and at most three surface.
- **Week-strip dots have no failure color**: DONE is filled, not-done is a
  neutral outline, unscheduled is blank. Missing a habit shortens a bar; it
  never turns anything red.
- **DB v3 -> v4** adds `habit` + `habit_completion` (composite PK); habit
  deletion cascades completions inside one transaction.

## 19. Mental health toolkit (Phase C)

- **The SOS button gets its destination.** Since the foundation, the amber
  center button has pushed `SosRoute`; that route was a placeholder until
  now. `ToolkitScreen` is what it always pointed to: breathing (hero, one
  tap), grounding, muscle release, a reframe shortcut into the thought
  record editor, and "remember your why" — reading the user's own
  onboarding reasons back to them. One footer line is explicit that these
  are self-help tools, not a substitute for professional support.
- **Breathing auto-starts.** A settings screen before help is friction
  nobody in a spike needs. Three patterns (Calm 4-6 extended-exhale
  default, Box 4-4-4-4, Relax 4-7-8) switch mid-session. The state machine
  (`advanceOneSecond`) is pure and unit-tested — the ViewModel supplies a
  1 Hz clock and nothing else, so phase transitions and cycle counting are
  tested without coroutines or timing flakiness.
- **The circle animates over the exact phase duration**, so the visual IS
  the pacing instruction, not decoration next to it. Reduce-motion holds a
  static circle at fixed scale; the countdown number still carries the
  guidance.
- **Sessions ≥30s log a BREATHING_SESSION journey event — zero migration.**
  `JourneyEventType` is a closed enum on an already-append-only table; a new
  case is the entire schema change. AD-1 (event sourcing) paying rent
  exactly as designed: the insight engine can pick this signal up later
  without touching storage.
- **Grounding and muscle release are self-paced**, advanced by the user, no
  per-step timers. Racing someone anxious against a countdown works against
  the exercise; a Next button doesn't.
- **CBT thought record and gratitude are first-class Journal entry kinds**,
  not text templates inside free entries — structured fields (situation,
  thought, feeling, intensity, reframe / three good things) stay queryable
  for any future insight that wants them, the same reasoning that keeps
  plan items and habits as codes rather than strings.
- **Journal is now a hub**: one merged, sorted timeline across free/thought/
  gratitude, each row kind-badged; the FAB opens a chooser bottom sheet
  instead of guessing which kind "new" means.
- **Reframe's reframe field is optional at save time.** Naming the thought
  and the feeling is already most of the exercise; forcing an answer before
  someone has one would turn a coping tool into a quiz.
- **Gratitude requires only the first entry.** Three is the classic
  exercise's target, not its gate.
- **DB v4 -> v5** adds `thought_record` + `gratitude_entry`. No change to
  `journey_event`'s schema for the new event type — see above.

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

## 20. Home-screen widget (Phase D, part 1)

- **A new top-level `widget/` package**, sibling to `feature/`, `data/`,
  `domain/`. A Glance `GlanceAppWidget` is not a nav-graph screen (it
  doesn't belong under `feature/`) and it's a rendering surface, not a data
  concern — it earns its own home.
- **The cache is deliberately unencrypted**, unlike every other user-data
  store (§5, §14) — the ONE intentional exception to "user data lives only
  in the encrypted DB." This is not a privacy regression: the two numbers
  cached (days clean, milestone target) are exactly what the widget renders
  in cleartext on the home screen the instant the user places it.
  Encrypting a cache of a number already shown in the open protects
  nothing. Everything with actual content — journal text, thought records,
  feelings, triggers — stays exclusively in SQLCipher and never reaches this
  file. `WidgetSnapshotStore`'s doc comment carries this reasoning verbatim
  so it reads as a decision, not an oversight.
- **The widget reads the same DataStore file via its own delegate, not
  dependency injection.** `GlanceAppWidget` instances are recreated on every
  update and are meant to stay stateless (Google's own guidance); rather
  than fight that with Hilt EntryPoints or lifecycle-timing workarounds,
  `ClarityWidget` declares its own `private val Context.widgetSnapshotDataStore`
  with the identical file name `WidgetSnapshotStore` uses. DataStore
  coordinates multiple same-named delegates safely — this is the documented,
  intended pattern for sharing a DataStore between an app and its widget,
  not a workaround.
- **Refresh reuses, rather than reinvents, the reminder system's scheduling
  primitives.** `WidgetRefreshScheduler` is a second inexact, self-chaining
  `AlarmManager` alarm (same rationale as §18: exact alarms are
  Play-restricted, WorkManager's periodic drift is wrong for "at a specific
  local time"), and it calls the SAME tested `ReminderTimeCalculator` habit
  reminders already use — new scheduling math was never needed.
- **One method, four call sites.** `WidgetSyncRepository.refresh()` recomputes
  the true streak via the existing `StreakCalculator`, writes the cache,
  calls Glance's `updateAll`, AND re-arms its own next alarm — all in one
  call, mirroring the reminder receiver's fire-then-rechain idiom. It's
  invoked from app start, boot, the daily alarm firing, and the moment
  onboarding completes (so a widget placed right after onboarding shows Day
  1 immediately rather than waiting for the next midnight).
- **`exported="true"` on the widget's receiver, `exported="false"` on
  everything else that listens for a broadcast.** This looks inconsistent
  until you know why: `BOOT_COMPLETED` is a protected system broadcast — the
  OS can deliver it to a non-exported receiver regardless, since `exported`
  only gates whether *other apps* could also trigger it (verified against
  Android Developer Relations' own guidance before trusting this, given how
  much noise exists on this exact question online). `APPWIDGET_UPDATE`, by
  contrast, is delivered by the launcher/AppWidgetHost — a genuinely
  different app/process — which needs ordinary exported access, matching
  Google's own Glance sample exactly.
- **No determinate circular progress in Glance 1.1.0** — RemoteViews has no
  arbitrary Canvas, so the app's Home ring can't be reproduced as-is. The
  large widget size uses a horizontal `LinearProgressIndicator` toward the
  milestone instead: an honest adaptation to the platform, not a downgrade
  nobody decided on.
- **Colors are hand-mirrored from `Color.kt`, not pulled from
  `glance-material3`.** Glance's own team recommends hard-coded
  `ColorProvider(day, night)` values for apps with a custom, non-dynamic
  palette (exactly this app's stance since §9) — pulling in the interop
  artifact to reuse `MaterialTheme` would fight that, not honor it.
  `WidgetColors.kt` is intentionally small and flags itself for updates if
  the app palette ever changes.
- **Tap opens the app to its normal start destination — no deep link into
  a specific screen.** Deep-linking the widget straight into breathing or
  SOS was considered and deliberately deferred: it's a genuinely separate
  feature (intent-filter design, NavHost deep-link wiring, testing) with its
  own risk surface, not a natural extension of this one.

## 21. Motion tokens & animation polish (Phase D, part 2)

- **`MotionTokens`** (durations: QUICK/STANDARD/EMPHASIZED/SETTLE, plus a
  shared easing) is the timing equivalent of `Spacing` — named values instead
  of every screen hand-rolling its own millisecond constants. New Phase-D
  motion reads from it; a handful of clearly-equivalent existing call sites
  were left as bare numbers on purpose (retrofitting every already-shipped
  animation for cosmetic consistency was judged not worth the compile risk
  of touching working code this late, with no compiler in the loop — noted
  here as a deliberate non-goal, not an oversight).
- **Habit done-toggle** now pops its checkmark the same way onboarding's
  `OptionCard` does (fade + scale-in) instead of appearing instantly — the
  two "you chose this" moments in the app now share one motion language.
- **The week chart's bars grow in from zero on first view**, then transition
  smoothly (never replaying from zero) as habit toggles change the data —
  an explicit `Animatable` + `LaunchedEffect(Unit)` per bar, since plain
  `animateFloatAsState` initializes directly at its target with no animation
  on first composition (correct, standard behavior — worth naming since it's
  the exact subtlety that makes the pre-existing streak-ring "sweep" mostly
  invisible on a cold app open too, and deliberately left as-is there: a
  screen opened dozens of times a day should show the correct number
  instantly, not make you wait through a sweep every single time).
- **Mood selection in the check-in sheet** now animates its border/fill/icon
  tint instead of snapping, the same quiet-transition idiom the bottom nav's
  selected tab already used.
- **The SOS toolkit hub** settles in with a staggered fade-and-rise entrance
  across its hero card and four tool cards — arguably the single screen most
  worth feeling calm and considered, since it's the one someone opens
  mid-crisis.
- **The streak ring's arc finally uses `ExtendedColors.celebration`** —
  defined in the very first foundation build, never used until now. Once the
  milestone is reached the arc renders in dawn-amber instead of teal, with
  the transition animated for the rare case it happens live. Deliberately
  NOT a looping or pulsing celebration: this is one of the most-viewed
  screens in the app, and a perpetual animation would read as gamified
  rather than premium by the tenth time someone sees it. A persisted
  "have I celebrated this milestone before" flag was considered and
  rejected for the same reason plus real complexity it doesn't earn yet
  (there's no relapse-recording UI to reset it against): the color
  difference is the actual payoff, correct even on a cold app open with no
  transition to watch.

## 22. Relapse & recovery (Phase E)

The most emotionally consequential surface in the app. Several choices here
deliberately depart from a purely literal reading of the brief, in the same
direction the app has leaned since onboarding: less friction, less shame,
more honesty about what's actually being computed.

- **The relapse entry point is a quiet text link under the streak, not a
  button.** A prominent always-visible "log a relapse" CTA would itself work
  against the point — it would make the screen you open to feel good about
  your progress also a daily reminder that failure is one tap away.
  Findable in one tap; never the loudest thing on the screen.
- **The relapse is recorded the instant it's confirmed, before the recovery
  flow even opens.** The five-step flow that follows is entirely supportive
  — closing the app mid-flow must never leave the streak half-reset. This
  mirrors onboarding's "persist first, let observed state drive what happens
  next," and means the flow's own failure to complete has zero effect on
  data correctness.
- **Every reflection field is optional**, exactly like check-ins, gratitude,
  and thought records already are. Forcing detailed introspection
  immediately after a setback can itself feel like an interrogation.
  Reflection reuses `MainTrigger`, `UrgeTime`, and `MoodLevel` verbatim
  rather than inventing near-duplicate enums — one `RelapseLocation` enum is
  the only genuinely new vocabulary this step needed.
- **The checklist is a third deterministic generator**, same contract as the
  onboarding plan and the weekly insights: pure, tested, typed codes (never
  raw strings), five universal foundations plus up to three personalized
  additions from what Reflection offered. Checkmarks are deliberately
  ephemeral (not persisted) — this is a one-time show of momentum, not a
  habit tracker; two items deep-link to the real Breathing and Journal
  screens rather than just describing them in text.
- **`StreakSnapshot` gained four fields** (`previousRunDays`,
  `bestClosedRunDays`, `totalRelapses`, `totalCleanDays`) — all derived
  inside the same walk `StreakCalculator` already did for `longestDays`,
  none of it new state. `previousRunDays` (the run that just ended) and
  `bestClosedRunDays` (the record to beat) are deliberately two different
  numbers, matching the Rebuild System's own "Previous streak" vs "Best
  streak" distinction — collapsing them into one would have been wrong the
  moment someone's most recent run isn't their longest one.
- **The Rebuild System is a state machine with exactly one derived
  boolean.** `isRebuilding` (`totalRelapses > 0 && !hasBeatenPreviousRecord`)
  drives everything: the ring's label swaps from "days clean" to "Recovery
  Day," the caption swaps to the two-stat framing, and the Motivation
  Engine's line appears. The moment `hasBeatenPreviousRecord` flips true,
  every one of those reverts automatically to the original framing — no
  separate transition to code, because `isRebuilding` becoming false *is*
  the transition.
- **The streak ring's celebration color (§21) now also fires on beating a
  previous record**, not just the onboarding milestone — both are genuinely
  worth the same visual treatment. Still no looping animation, same
  reasoning as before: this screen is opened many times a day.
- **Recovery Score weights lifetime clean rate at 60%, current momentum at
  20% (capped at 30 days), and proven capability at 20% (capped at 90 days,
  echoing the reboot framing already used at onboarding).** The weighting is
  deliberately shaped so a strong history doesn't collapse after one new
  relapse, and so a brand-new user with a perfect record doesn't see a
  falsely low number just for being new. The score was NOT tuned to
  reproduce this brief's illustrative "84%" example exactly — that number
  reads as flavor text, not a spec to reverse-engineer, and contorting the
  formula to hit one example would have made it less defensible everywhere
  else.
- **Comeback achievements are computed live from `StreakSnapshot`, with zero
  persisted "unlocked" state** — same "derive, don't store" discipline as
  the streak itself, which sidesteps an entire class of sync bugs. The
  honest trade-off: they describe *this* comeback, not a lifetime
  collection. Relapse again, and they recompute fresh for the new one —
  treated as thematically correct (each comeback earns its own pride), not
  a limitation, and it avoids real complexity a lifetime model would need
  with no unlock-tracking table to match it against.
- **DB v5 -> v6** adds `relapse_reflection`, linked to its `journey_event`
  row by id (`JourneyRepository.record()` now returns the new row's id —
  the one interface signature change this phase, verified against its
  single existing caller before making it).
- **Engineering note for future contributors touching these ViewModels**:
  `kotlinx.coroutines`'s `combine()` has no overload past 5 named flow
  arguments (confirmed against the maintainers' own issue tracker, not
  assumed). `HomeViewModel` and `JourneyViewModel` both fold extra UI-only
  flows into one `Triple`/pair-producing inner `combine` to stay under that
  ceiling — reach for the same trick before reaching for a vararg array.

## 23. Banner ads via AdMob (post-Phase-E)

This is the one deliberate exception to the "no internet, ever" invariant
maintained since the foundation build, added after an explicit trade-off
conversation with the developer rather than silently. It's recorded here in
the same spirit as every other decision in this log: not because it was
free of cost, but so the cost is visible to whoever reads this next.

- **What changes and what doesn't**: INTERNET, ACCESS_NETWORK_STATE, and
  AD_ID are now requested, used exclusively to serve a small banner and to
  gather UMP consent before doing so. Nothing about the encrypted Room
  database, the journal, thought records, or check-ins changes — none of
  that data has any path to the network, today or after this change.
- **The real risk this doesn't fully solve**: restricting ads to non-
  sensitive screens protects someone from an ad appearing mid-relapse; it
  does not prevent Google's ad-tech pipeline from learning that a given
  device runs a porn-recovery app at all, which happens the moment the SDK
  initializes and any ad request goes out, regardless of which screen
  triggered it. This was surfaced to the developer explicitly before
  implementation, along with a recommendation to review AdMob's current
  sensitive-content policy before shipping to production — that review is
  the developer's to do; this codebase can't verify policy compliance for
  them.
- **The allow-list is enforced by type, not by a runtime check.**
  `domain/ads/AdScreen` is a closed enum containing exactly the screens
  ads may appear on. A screen that must never show ads simply has no
  `AdScreen` value to construct — there is nothing to remember to exclude.
  This is deliberately the opposite shape from a deny-list, where any
  future screen would default to showing ads unless someone remembered to
  add it to an exclusion set. Given the subject matter, fail-closed is the
  only direction that makes sense.
- **Current stable SDK, not the Next-Gen rewrite.** Google announced a
  rewritten "GMA Next-Gen SDK" in January 2026; as of this integration its
  latest published version is explicitly `0.24.0-beta01` — beta, not
  something to build a monetization path on. This integrates the current
  stable `play-services-ads:23.6.0` line instead (confirmed against
  Google's own documentation, not memorized).
- **Consent before initialization, always.** `AdsManager` sequences UMP's
  `requestConsentInfoUpdate()` → `loadAndShowConsentFormIfRequired()` →
  only then `MobileAds.initialize()`, on every launch. `canRequestAds()`
  and the privacy-options requirement are both exposed as `StateFlow`, not
  plain functions — consent resolution is asynchronous, and a banner gated
  on a one-time synchronous read would never appear if consent resolved
  after the banner's first composition.
- **No Settings screen exists in this app**, so the UMP-required
  privacy-options entry point lives as a small quiet link on Home instead
  (visible only when UMP reports one is actually required) — the same
  restrained visual treatment as the relapse entry link, not a new feature
  in its own right.
- **`isPremiumUser` is a real, wired, always-false stub** — a DataStore-
  backed flow with no setter, since no billing/entitlement system exists
  yet. This exists so `AdsManager`'s premium gate has a stable interface
  from day one rather than needing a rewiring pass whenever billing lands.
- **Fixed-size banner, not adaptive.** Google's current guidance favors
  anchored adaptive banners; this ships the simpler fixed `AdSize.BANNER`
  as a deliberate first pass, since adaptive sizing needs live
  screen-width measurement this environment has no way to verify without a
  device. A reasonable, non-urgent upgrade later.
- **Pure logic extracted and tested as usual**: `AdPolicy` (the allow-list
  premium gate and the debug/release ad-unit-id selection) has zero
  Android or AdMob dependency and is unit tested exactly like every other
  decision engine in this app. Everything UMP/SDK-specific lives in
  `AdsManager`, which cannot be meaningfully unit tested (it needs a real
  device, network, and Google's ad servers) — the same honest boundary
  this project has drawn around every platform integration so far
  (widgets, notifications).

## 24. Premium architecture & Settings (post-Phase-D monetization pass)

Prepares the app for Google Play Billing without implementing purchases —
the explicit brief for this pass. Everything here is a real, wired seam,
not aspirational scaffolding: every interface created has a genuine
implementation bound through Hilt today, even where that implementation is
honestly a no-op.

- **`isPremiumUser` moved off `SettingsRepository` entirely.** It was
  added there as an explicit, documented placeholder when banner ads first
  shipped ("no setter yet... would need rewiring later" — that rewiring is
  this section). It's superseded by a dedicated `domain/premium/` stack
  with its own DataStore file (`PremiumPreferences`, `clarity_premium`),
  keeping the premium/billing subsystem self-contained rather than
  entangled with unrelated app settings. This is the one deliberate
  departure from "don't rewrite existing architecture" in this pass,
  called out explicitly for the same reason every other trade-off in this
  log is: it completes something already flagged as temporary, rather than
  disturbing something that was meant to last.
- **`PremiumState` is a sealed type, not a raw Boolean.** Today the app
  only distinguishes Free vs Premium, but this is exactly the shape of flag
  that needs to grow (a trial, an expiring subscription, a pending
  purchase) — a sealed type lets new cases arrive later without changing
  every existing consumer's signature.
- **The six pieces map to five small classes, not six** — `PremiumState`
  is a type, not a class with behavior, so "Global premium state" doesn't
  need its own separate holder: `PremiumManager.premiumState` (a
  `@Singleton`, matching every other manager/repository's scope in this
  app) *is* the global state, the one property everything else in the app
  depends on.
- **`BillingConnector` has zero Play Billing types in its signature** —
  same "domain stays platform-free even though the only consumer is a
  platform surface" reasoning as `WidgetSyncRepository`. Its only
  implementation, `NoOpBillingConnector`, is genuinely bound via Hilt and
  genuinely injected into `PremiumManager` (via `refreshFromBilling()`),
  not merely declared-and-ignored — an interface with a binding nothing
  ever calls is exactly the kind of orphaned placeholder this pass exists
  to avoid repeating.
- **`AdPolicy.isAdsAllowed(screen, isPremiumUser: Boolean)` needed zero
  changes.** It already took a plain boolean, decoupled from where that
  boolean came from — this is the whole reason "one Premium boolean hides
  every ad" was already true by construction once `BannerAdViewModel` was
  pointed at `PremiumManager` instead of the old settings stub. The
  premium-gating logic itself was never the thing that needed building;
  only its source of truth was.
- **`ClarityBannerAd` now tracks real load state.** Previously it rendered
  its `AndroidView` container the moment policy allowed an attempt,
  regardless of whether an ad actually rendered — a network failure left a
  blank box in the layout. An `AdListener` now drives `hasLoadedAd`;
  nothing is shown until `onAdLoaded` fires, and `onAdFailedToLoad`
  collapses the banner back to zero space. The reveal itself animates in
  (fade + slight rise, `MotionTokens.STANDARD`) rather than popping in.
  Placement is unchanged (inline within scrolling content, never a fixed
  overlay) — this is what already satisfied "respects navigation bars" and
  "never overlaps content" without needing a different, riskier pattern.
- **`AdScreen.SETTINGS` was added exactly the way the enum's own doc
  comment describes**: a one-line addition when Settings shipped, not a
  restructuring of the allow-list's shape.
- **Settings is reached via a gear icon on Home, not a sixth bottom-bar
  tab.** `TopLevelDestination` and `ClarityBottomBar`'s layout are
  hardcoded for exactly four tabs split 2+2 around the center SOS button;
  adding a fifth tab would have forced a rework of that split, which is
  precisely the kind of existing-architecture rewrite this pass was asked
  to avoid. Settings pushes as an ordinary back-stack destination instead
  — the bottom bar already hides for any non-`TopLevelDestination` route,
  so no change was needed there either.
- **Settings is deliberately minimal**: a Premium section and nothing
  else. `SettingsRepository.themeMode`/`setThemeMode` has existed with no
  UI anywhere since the foundation build — the same orphaned-placeholder
  shape `isPremiumUser` was in — and this screen would be the natural home
  for it, but that's out of scope for what was asked here and is left as
  an explicit, flagged follow-up rather than bundled in silently.

## 25. A more precise rule for the AnimatedVisibility/DslMarker trap

Two separate CI failures now trace to the same underlying issue, and the
second occurrence revealed the first rule (§16, §21: "don't put
`AnimatedVisibility` in a `Box` directly inside a `Row`/`Column`") was too
narrow. The precise rule:

**The collision happens when `Row`/`Column` and `Box` are both open in the
same lexical function body, no matter what sits between them** — including
non-scope-introducing wrappers like `Surface` or `ClarityCard`, which don't
shadow an enclosing `RowScope`/`ColumnScope`. `ChecklistRow`'s
`Row { Surface { Box { AnimatedVisibility } } }` broke for exactly this
reason: `Surface` introduces no scope of its own, so `RowScope` stayed
visible all the way down to the `AnimatedVisibility` call, colliding with
the `BoxScope` right above it.

**What actually makes it safe is a function-call boundary, not visual
nesting.** `DoneToggle`'s identical-looking `Surface { Box { AnimatedVisibility } }`
is fine because `DoneToggle` is called as a separate function from its
caller's `Row` — a function call resets which implicit receivers are
visible (unless the function's own signature declares a receiver, e.g.
`fun RowScope.Foo(...)`), so the caller's `RowScope` never reaches inside.
Writing the same three composables directly inline within one function
body, with a `Row` anywhere in its enclosing lexical scope, does not get
that reset.

The fix in both cases is the same and cheaper than restructuring: replace
`AnimatedVisibility` with a plain `if (condition) { ... }` wherever this
shape occurs. A brace-depth-aware checker (tracking `Row`/`Column`/`Box`
opens and closes per function body, resetting the stack at each new
`fun` boundary unless it declares a scope receiver) now exists and passes
clean across the whole project — a meaningful upgrade from the earlier
fixed-line-window heuristic, which is exactly what missed this instance
the first time.

## 26. Process note: sweep every construction site when a data class grows

`StreakSnapshot` gained four new required fields in §22. I updated the one
test file I was directly editing at the time (`StreakCalculatorTest`) and
verified the real production call site (`StreakCalculator.compute`), but
never grepped the whole project for *other* test files constructing
`StreakSnapshot(...)` by hand. `InsightGeneratorTest.kt` — written back in
Phase B, before those fields existed — had four positional-only call sites
that only compiled because Kotlin data class constructors are positional
by default; adding required fields silently broke every one of them the
moment CI actually compiled the test source set.

The fix: a small `streakSnapshot(currentDays, longestDays, cleanSinceEpochDay)`
helper local to that file, supplying inert defaults for the four newer
fields (`InsightGenerator` itself only ever reads `currentDays` — confirmed
by reading its source before assuming the defaults were safe, not after).

The durable lesson: **when a shared data class gains required constructor
parameters, grep the entire tree (`grep -rn "TypeName("`) for every
construction site, not just the files already open for the change at
hand.** `main` and `test` are compiled as separate source sets by Gradle,
which is exactly why `compileDebugKotlin` can go green while
`compileDebugUnitTestKotlin` fails right after — a clean main build is not
evidence the test tree still compiles.

## 27. Google Play Billing v8 (remove_ads, one-time purchase)

The domain contracts, `PlayBillingConnector`, `PurchaseResult`, and the
`PremiumManager`/`ClarityApp` wiring already existed when this pass began —
found by inspection, not built from memory of an earlier session, and
verified rather than assumed correct. The one real gap: `SettingsScreen.kt`
still referenced a `premiumState` property that no longer existed on
`SettingsViewModel` and had a non-exhaustive `when` missing the `Pending`
case — it would not have compiled. That screen is this section's actual
new work; everything below it in this list already existed and was
confirmed correct by reading it, not by re-deriving it from scratch.

- **`PremiumState` gained a third case, `Pending`**, alongside `Free` and
  `Premium` — distinct from `PurchaseResult.Pending`, which is the outcome
  of *one attempt*, not durable ownership status. Cash and bank-transfer
  payment methods (common in Japan, Brazil, Indonesia, and elsewhere) take
  time to clear; a pending purchase must not unlock premium yet.
  `PremiumState.isPremium` is deliberately true for `Premium` alone.
- **`PendingPurchasesParams.enableOneTimeProducts()` is called explicitly.**
  Billing v8 stopped implying this from the deprecated no-arg
  `enablePendingPurchases()`; omitting it silently breaks pending-purchase
  handling for exactly the payment methods above.
- **Client-side signature verification, fail-closed.** No cloud backend
  (by requirement) means no server-side purchase-token verification either
  — the honest trade-off documented directly in `PlayBillingConnector`:
  this stops casual tampering, not a sufficiently motivated attacker with
  root on their own device. The placeholder public key is checked first;
  every purchase is rejected as unverified until the real Play Console
  licensing key replaces it, so forgetting to configure this cannot
  silently grant free premium to everyone — it fails safe, not open.
- **"Survives reinstall" has nothing to do with local storage.**
  `PremiumManager.refreshFromBilling()`, called from `ClarityApp.onCreate()`,
  queries Google's own purchase records at every startup. The purchase is
  still valid on Google's servers even when nothing survived locally after
  a reinstall — this is the entire mechanism, and it needed no changes.
- **`PurchaseResult` and `PremiumState` are deliberately two different sealed
  types.** Cancelling a purchase attempt doesn't change what you own; it's
  an event the attempt produced, not a new ownership state — collapsing
  them into one type would have made `PremiumState` need a `Cancelled` case
  that persisted nonsensically.
- **`AdsManager` was not touched, at all, in this entire pass** — confirmed
  by inspection before writing anything, not assumed. `BannerAdViewModel`
  already depended on `PremiumManager.isPremium`, itself sourced from
  `PremiumState.isPremium` (true only for `Premium`, never `Pending`), so
  "remove every banner instantly" and "a pending purchase must not hide
  ads yet" were both already correct, structurally, before this pass began.
- **The Settings UI fix**: `SettingsScreen` now actually reads
  `SettingsViewModel.uiState` (not a nonexistent property), handles all
  three `PremiumState` cases via a small pill-shaped `StatusBadge`, and
  wires both buttons to real actions. The "smooth success animation"
  requirement is carried entirely by `AnimatedContent`'s state-driven
  crossfade on that badge — no separate celebration mechanism was built,
  since the badge appearing already *is* the celebration, and it costs
  nothing extra to wire. `AnimatedContent` was confirmed (directly against
  its documented overloads, not assumed from `AnimatedVisibility`'s
  behavior) to have no `RowScope`/`ColumnScope`-specific overload to
  collide with, unlike `AnimatedVisibility` — so nesting it inside a `Row`
  here carries none of the DslMarker risk documented in §25.
- **Every non-success `PurchaseResult` gets exactly one piece of feedback,
  once.** `Cancelled` gets nothing — cancelling is an ordinary choice, not
  a failure worth interrupting anyone over. Every other outcome
  (`Pending`, `BillingUnavailable`, `NothingToRestore`, `Error`) gets a
  brief `Snackbar`, shown once via the existing one-shot
  `purchaseResult`-then-`onPurchaseResultConsumed()` pattern the ViewModel
  already implemented correctly.

## 28. Recovery flow redesign: What Happened / Feelings / Trigger

The single "Reflection" step (trigger + time-of-day + mood + location +
free notes, all in one scrollable form, reusing onboarding's `MainTrigger`/
`UrgeTime` and the daily check-in's `MoodLevel`) is replaced by three
focused, sequential steps with their own purpose-built vocabulary. The
flow is now Accept -> What Happened -> Feelings -> Trigger -> Learn ->
Plan -> Restart — seven phases, still one VM-internal state machine, same
reasons as §16/§22.

- **Three new domain enums, not reused generic ones.** `RelapseSetbackType`
  (Porn/Masturbation/Both/Urge only), `RelapseEmotion`
  (Guilty/Empty/Angry/Anxious/Hopeless/Okay), and `RelapseTrigger`
  (Stress/Loneliness/Social media/Boredom/Night/Couldn't sleep/Other) each
  get their own type rather than overloading `MoodLevel` or `MainTrigger`+
  `UrgeTime` to mean something they don't. `MoodLevel.STRUGGLING` standing
  in for "Guilty" would have been a lossy, confusing hack — daily-check-in
  mood and post-relapse emotion are genuinely different questions asking
  for genuinely different vocabulary.
- **`RelapseTrigger` deliberately flattens trigger and timing into one
  list.** NIGHT and COULDNT_SLEEP are nameable triggers here in their own
  right, not a time-of-day modifier on some other trigger — matching
  exactly how the question was actually asked, rather than forcing it back
  into the existing two-axis split.
- **Still nothing is required to continue.** Each of the three new steps
  is single-select with an always-enabled Continue, exactly like the
  reflection step it replaces — the redesign changes what's asked and how
  many screens it takes, not the standing principle that disclosure is
  offered, never gated.
- **`RelapseReflection`'s old `location` and free-text `notes` fields have
  no home in the new design and are dropped, not preserved.** This is a
  real simplification, not an oversight: the three specific questions
  requested don't include either. `RelapseLocation` becomes fully orphaned
  the moment its one caller stops using it and is deleted outright, along
  with its now-unused strings — an unused enum with zero remaining callers
  is dead code, not a placeholder worth keeping.
- **DB v6 -> v7 is a drop-and-recreate, not a column migration.** There's
  no meaningful way to carry old free-text notes or a location forward
  into fields that don't correspond to them. This only affects the
  optional reflection *color* on past relapses — the append-only
  `journey_event` record of each relapse (§17, §22) is completely
  untouched and remains the sole source of truth for streak math.
- **`RecoveryChecklistGenerator` collapsed its two-parameter signature
  `(trigger: MainTrigger?, timeOfDay: UrgeTime?)` into one:
  `(trigger: RelapseTrigger?)`.** `RelapseTrigger.NIGHT` and
  `.COULDNT_SLEEP` both route to the same wind-down checklist item —
  two enum values, one personalization outcome, no duplicated logic.
- **The Learn step's pattern-matching moved from a single nullable
  `matchesTrigger` plus a separate `matchesLateNight` boolean to one
  `matchesTriggers: Set<RelapseTrigger>` per card** — letting the
  "Night & sleeplessness" card highlight for either NIGHT or COULDNT_SLEEP
  without needing a second boolean flag bolted onto the data shape.
- **The Restart screen is now the actual motivational moment the plan
  described**, not just a plain restart button: "You proved you can reach
  N days" (shown only when a previous run exists — never invented for a
  first-ever relapse), then Start Again, then two shortcuts — Breathing
  and the Emergency Toolkit — reusing the exact deep-link pattern already
  proven on the Plan step (push on top, come back via the back button;
  tapping a shortcut does not implicitly finish the flow). Both shortcut
  icons reuse icons already confirmed compiling elsewhere in this exact
  project (`SelfImprovement`, `Psychology`) rather than introduce
  unverified new ones for a cosmetic choice.
- **The Restart screen switched from `weight()`-centered content to a
  scrollable Column.** It now carries meaningfully more content (the proof
  line, two extra buttons) than the version that fit comfortably centered
  on a fixed-height screen; `Modifier.weight()` inside `verticalScroll` is
  a real Compose anti-pattern (a scrolling container offers unbounded
  height, which weight has nothing sensible to divide), so this was
  rebuilt with fixed spacers instead of quietly risking that combination.

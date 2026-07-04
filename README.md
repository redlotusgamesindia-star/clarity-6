# Clarity — Foundation Build (Phase 1)

Private, offline-first recovery companion. Android 10+ (minSdk 29), Kotlin,
Jetpack Compose, Material 3, Hilt, Room (SQLCipher), DataStore, Navigation
Compose.

## Open & run

1. **Android Studio Ladybug (2024.2) or newer**, JDK 17.
2. Open the project root. If Studio asks about a missing Gradle wrapper JAR,
   let it regenerate it (or run `gradle wrapper` once with any local Gradle
   8.x) — the binary JAR is intentionally not committed.
3. Sync → Run. First launch shows the onboarding placeholder; Continue flips
   the persisted flag and drops you into the four-tab shell with the SOS
   button.

## What's real vs placeholder

Real: architecture, DI graph, encrypted DB wiring (now schema v2 with a
hand-written migration), settings persistence, theme/typography/dark mode,
design-system components, navigation shell, splash, and the **full
onboarding**: animated welcome, 11 questions, deterministic personalized
plan generation (unit-tested — run `./gradlew testDebugUnitTest`), and
profile+plan persistence in the encrypted database. Placeholder (loudly
labeled in-app): the four tab screens and SOS — next per the project plan.

## Notes

- **Fonts** (Fraunces/Inter) load via the Google Fonts provider — no app
  permissions involved. Offline/no-Play-services devices fall back to system
  serif/sans. Bundle TTFs in `res/font` before store submission (see
  `Type.kt` production note).
- **Zero permissions** by design; the manifest is the privacy policy.
- Architecture rationale: see `ARCHITECTURE.md`.

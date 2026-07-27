package com.runtimelabs.clarity.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable

/**
 * Named motion durations + shared easings, the timing equivalent of
 * [Spacing]. Before this, every animated screen hand-rolled its own
 * millisecond constants (200/300/350/360/900...) — harmless individually,
 * but it meant "does this feel consistent with the rest of the app" had no
 * single answer. New Phase-D motion reads from here; existing animations
 * are retrofit only where the swap is a trivial equivalent-value change
 * (documented in ARCHITECTURE.md §21) — a full retrofit of already-shipped,
 * already-tested motion is a deliberate non-goal for this pass.
 */
@Immutable
object MotionTokens {
    /** Micro-feedback: toggles, tint swaps, checkmark pops. */
    const val QUICK = 150

    /** The default for most transitions: sheets, tab content, crossfades. */
    const val STANDARD = 300

    /** Deliberately noticeable: onboarding beats, milestone moments. */
    const val EMPHASIZED = 500

    /** Data visualizations settling in: bars, rings, progress sweeps. */
    const val SETTLE = 900

    /**
     * One full inhale-exhale cycle for the "breathing glow" motif (2026
     * refresh's signature element — see
     * [com.runtimelabs.clarity.core.designsystem.components.BreathingIndicator]
     * and the ambient backdrop blobs). Matches a relaxed resting breath, same
     * cadence family as the pre-existing 1800ms breathing indicator, just
     * named here so every new glow-pulse in the app shares one rhythm
     * instead of drifting apart file by file.
     */
    const val BREATH = 4200

    /** Very slow ambient drift for background gradient blobs / particles — deliberately imperceptible as a "duration", just slow enough to never feel static. */
    const val AMBIENT_DRIFT = 14000

    /** Standard Material-ish deceleration; matches the feel already used app-wide. */
    val Standard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /** Gentle ease for slow ambient motion — no sharp acceleration, matches [BREATH]/[AMBIENT_DRIFT]'s calm intent. */
    val Ambient: Easing = CubicBezierEasing(0.37f, 0.0f, 0.63f, 1.0f)
}

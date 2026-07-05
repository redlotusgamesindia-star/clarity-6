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

    /** Standard Material-ish deceleration; matches the feel already used app-wide. */
    val Standard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
}

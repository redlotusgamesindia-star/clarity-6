package com.runtimelabs.clarity.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Single spacing scale for the whole app. Screens compose paddings from these
 * tokens instead of ad-hoc dp literals — the difference between "minimal" and
 * "sparse" is consistent rhythm.
 */
@Immutable
data class Spacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 36.dp,
    val xxl: Dp = 56.dp,
    /**
     * Default horizontal inset for screen content. Widened from 20dp as
     * part of the 2026 visual refresh ("more whitespace" is one of that
     * pass's explicit goals) — content now sits a little further from the
     * glass edges of floating cards, which reads as calmer and more
     * premium on a deep-black canvas than it did on a flat light surface.
     */
    val screenHorizontal: Dp = 24.dp,
    /**
     * The gap between a title and its subtitle directly beneath it — used
     * dozens of times across the app (Home, Journey, Achievements, badge
     * detail sheets) but never named until this polish pass, when the same
     * literal `2.dp` turned up independently in nine different files.
     * Named here so it reads as one deliberate rhythm choice, not nine
     * coincidentally-matching magic numbers.
     */
    val hairline: Dp = 2.dp,
    /**
     * The tight horizontal gap between a leading element (usually an icon,
     * sometimes a toggle or a label column) and what sits beside it in a
     * row — same "found repeated, then named" reasoning as [hairline].
     */
    val iconGap: Dp = 12.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

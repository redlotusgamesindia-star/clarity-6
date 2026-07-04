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
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
    /** Default horizontal inset for screen content. */
    val screenHorizontal: Dp = 20.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

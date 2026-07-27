package com.runtimelabs.clarity.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.domain.model.ThemeMode

/**
 * Soft, generous radii — part of the "calm" identity, now pushed further
 * into 2026-premium territory (24-32dp on primary surfaces) so nothing in
 * the app reads as a stock Material corner anymore. `large` is what
 * [com.runtimelabs.clarity.core.designsystem.components.ClarityCard] uses
 * by default, which is why it — not `extraLarge` — is the one that has to
 * land inside the 24-32dp band; `extraLarge` goes a step further for the
 * true hero surfaces (sheets, dialogs, celebration cards). Small utility
 * shapes (chips, tags) stay proportionally smaller on purpose — a 12dp chip
 * at a 32dp radius would just be a pill with extra steps, not "premium".
 */
val ClarityShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Maps the persisted [ThemeMode] to an effective dark/light decision. */
@Composable
fun shouldUseDarkTheme(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/**
 * App theme. Dynamic color is intentionally NOT wired: the cyan/violet neon
 * palette *is* the brand, and Material You would dissolve it into wallpaper
 * colors. Revisit as an opt-in setting if users ask.
 */
@Composable
fun ClarityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) ClarityDarkColorScheme else ClarityLightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalSpacing provides Spacing(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ClarityTypography,
            shapes = ClarityShapes,
            content = content,
        )
    }
}

/** `MaterialTheme.extended.celebration`, `MaterialTheme.spacing.md`, etc. */
val MaterialTheme.extended: ExtendedColors
    @Composable @ReadOnlyComposable get() = LocalExtendedColors.current

val MaterialTheme.spacing: Spacing
    @Composable @ReadOnlyComposable get() = LocalSpacing.current

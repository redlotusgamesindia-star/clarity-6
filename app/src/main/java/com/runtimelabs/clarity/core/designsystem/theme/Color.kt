package com.runtimelabs.clarity.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * Clarity palette — "deep teal & dawn".
 *
 * Design intent (plan §9): calm, premium, hopeful. Teal carries the identity;
 * dawn amber is reserved for celebration and the SOS action. There is
 * deliberately NO alarm red anywhere in the system — even `error` is a muted
 * clay tone, because this app must never feel punitive.
 */

// -- Light --
private val TealPrimary = Color(0xFF0E7C7B)
private val TealOnPrimary = Color(0xFFFFFFFF)
private val TealPrimaryContainer = Color(0xFFCCE8E7)
private val TealOnPrimaryContainer = Color(0xFF00302F)

private val SeaSecondary = Color(0xFF4E7B7A)
private val SeaOnSecondary = Color(0xFFFFFFFF)
private val SeaSecondaryContainer = Color(0xFFCFE7E6)
private val SeaOnSecondaryContainer = Color(0xFF0B2F2E)

private val DawnTertiary = Color(0xFFA36833)
private val DawnOnTertiary = Color(0xFFFFFFFF)
private val DawnTertiaryContainer = Color(0xFFFFDCBF)
private val DawnOnTertiaryContainer = Color(0xFF2E1600)

private val ClayError = Color(0xFF9C4238)
private val ClayOnError = Color(0xFFFFFFFF)
private val ClayErrorContainer = Color(0xFFF3DAD6)
private val ClayOnErrorContainer = Color(0xFF3B0905)

private val MistBackground = Color(0xFFF7F9F9)
private val InkOnBackground = Color(0xFF1B2B2B)
private val PaperSurface = Color(0xFFFEFFFF)
private val InkOnSurface = Color(0xFF1B2B2B)
private val FogSurfaceVariant = Color(0xFFDCE5E4)
private val SlateOnSurfaceVariant = Color(0xFF3F4948)
private val StoneOutline = Color(0xFF6F7978)

// -- Dark --
private val TealPrimaryDark = Color(0xFF7FD1CF)
private val TealOnPrimaryDark = Color(0xFF003736)
private val TealPrimaryContainerDark = Color(0xFF00504F)
private val TealOnPrimaryContainerDark = Color(0xFF9FF1EF)

private val SeaSecondaryDark = Color(0xFFB0CCCB)
private val SeaOnSecondaryDark = Color(0xFF1B3534)
private val SeaSecondaryContainerDark = Color(0xFF324B4A)
private val SeaOnSecondaryContainerDark = Color(0xFFCCE8E7)

private val DawnTertiaryDark = Color(0xFFF0B98E)
private val DawnOnTertiaryDark = Color(0xFF4A2800)
private val DawnTertiaryContainerDark = Color(0xFF6A3F12)
private val DawnOnTertiaryContainerDark = Color(0xFFFFDCBF)

private val ClayErrorDark = Color(0xFFE0A79F)
private val ClayOnErrorDark = Color(0xFF4A211B)
private val ClayErrorContainerDark = Color(0xFF6B342C)
private val ClayOnErrorContainerDark = Color(0xFFF6DDD9)

private val NightBackground = Color(0xFF0D1414)      // near-black teal, OLED-friendly
private val MistOnBackground = Color(0xFFDDE4E3)
private val DeepSurface = Color(0xFF132020)
private val MistOnSurface = Color(0xFFDEE4E3)
private val DeepSurfaceVariant = Color(0xFF1E2E2D)
private val FogOnSurfaceVariant = Color(0xFFBEC9C8)
private val AshOutline = Color(0xFF889392)

val ClarityLightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = TealOnPrimary,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = TealOnPrimaryContainer,
    secondary = SeaSecondary,
    onSecondary = SeaOnSecondary,
    secondaryContainer = SeaSecondaryContainer,
    onSecondaryContainer = SeaOnSecondaryContainer,
    tertiary = DawnTertiary,
    onTertiary = DawnOnTertiary,
    tertiaryContainer = DawnTertiaryContainer,
    onTertiaryContainer = DawnOnTertiaryContainer,
    error = ClayError,
    onError = ClayOnError,
    errorContainer = ClayErrorContainer,
    onErrorContainer = ClayOnErrorContainer,
    background = MistBackground,
    onBackground = InkOnBackground,
    surface = PaperSurface,
    onSurface = InkOnSurface,
    surfaceVariant = FogSurfaceVariant,
    onSurfaceVariant = SlateOnSurfaceVariant,
    outline = StoneOutline,
)

val ClarityDarkColorScheme = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = TealOnPrimaryDark,
    primaryContainer = TealPrimaryContainerDark,
    onPrimaryContainer = TealOnPrimaryContainerDark,
    secondary = SeaSecondaryDark,
    onSecondary = SeaOnSecondaryDark,
    secondaryContainer = SeaSecondaryContainerDark,
    onSecondaryContainer = SeaOnSecondaryContainerDark,
    tertiary = DawnTertiaryDark,
    onTertiary = DawnOnTertiaryDark,
    tertiaryContainer = DawnTertiaryContainerDark,
    onTertiaryContainer = DawnOnTertiaryContainerDark,
    error = ClayErrorDark,
    onError = ClayOnErrorDark,
    errorContainer = ClayErrorContainerDark,
    onErrorContainer = ClayOnErrorContainerDark,
    background = NightBackground,
    onBackground = MistOnBackground,
    surface = DeepSurface,
    onSurface = MistOnSurface,
    surfaceVariant = DeepSurfaceVariant,
    onSurfaceVariant = FogOnSurfaceVariant,
    outline = AshOutline,
)

/**
 * Semantic colors Material's scheme has no slot for. Success/warning stay
 * muted; [celebration] is the dawn-amber used for milestones and the SOS
 * button — the one place the palette is allowed to be loud.
 */
@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccessContainer: Color,
    val successContainer: Color,
    val warning: Color,
    val celebration: Color,
)

val LightExtendedColors = ExtendedColors(
    success = Color(0xFF3E7D5A),
    successContainer = Color(0xFFD3EEDD),
    onSuccessContainer = Color(0xFF0E2E1D),
    warning = Color(0xFF9A7A2E),
    celebration = Color(0xFFE8A87C),
)

val DarkExtendedColors = ExtendedColors(
    success = Color(0xFF8FD4AB),
    successContainer = Color(0xFF244834),
    onSuccessContainer = Color(0xFFC9F0D8),
    warning = Color(0xFFE8C468),
    celebration = Color(0xFFF0B98E),
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

package com.runtimelabs.clarity.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * Clarity palette — "neon minimal" (2026 visual refresh).
 *
 * Design intent: a premium, futuristic dark surface — near-black with a
 * soft blue/cyan glow and a violet counter-accent, in the spirit of
 * Linear / Arc / Nothing rather than a clinical health-app teal. The
 * warm amber `tertiary` is the one deliberate exception to the cyan/violet
 * pair: it is inherited, on purpose, from the previous palette's rule that
 * the SOS button and milestone celebrations are the ONLY loud moments in
 * the app ("the one place the palette is allowed to be loud" — original
 * doc comment). Neon-on-black makes that restraint even more important, so
 * the rule carries forward unchanged: cyan/violet everywhere ordinary,
 * warm amber only for "you reached for help" and "you hit a milestone".
 * `error` stays a muted rose, never red, for the same non-punitive reason
 * as before — a relapse is not a failure state to alarm someone about.
 */

// -- Light (secondary mode — same language, tuned for a white surface) --
private val ElectricBlue = Color(0xFF0E7FB0)
private val OnElectricBlue = Color(0xFFFFFFFF)
private val ElectricBlueContainer = Color(0xFFCFEFFF)
private val OnElectricBlueContainer = Color(0xFF00344A)

private val VioletSecondary = Color(0xFF7449D6)
private val OnVioletSecondary = Color(0xFFFFFFFF)
private val VioletSecondaryContainer = Color(0xFFE9DDFF)
private val OnVioletSecondaryContainer = Color(0xFF2A1157)

private val AmberTertiary = Color(0xFFB85E1D)
private val OnAmberTertiary = Color(0xFFFFFFFF)
private val AmberTertiaryContainer = Color(0xFFFFDCBC)
private val OnAmberTertiaryContainer = Color(0xFF3D1D00)

private val RoseError = Color(0xFFB33A55)
private val OnRoseError = Color(0xFFFFFFFF)
private val RoseErrorContainer = Color(0xFFFFD8DF)
private val OnRoseErrorContainer = Color(0xFF400012)

private val CloudBackground = Color(0xFFF3F6FB)
private val InkOnBackground = Color(0xFF15181E)
private val PaperSurface = Color(0xFFFFFFFF)
private val InkOnSurface = Color(0xFF15181E)
private val MistSurfaceVariant = Color(0xFFE7ECF5)
private val SlateOnSurfaceVariant = Color(0xFF565F70)
private val StoneOutline = Color(0xFFAEB6C4)

// -- Dark (flagship mode) --
private val NeonCyan = Color(0xFF4FC8FF)
private val OnNeonCyan = Color(0xFF00232E)
private val NeonCyanContainer = Color(0xFF0E3A4D)
private val OnNeonCyanContainer = Color(0xFFBEE9FF)

private val NeonViolet = Color(0xFFC3A3FF)
private val OnNeonViolet = Color(0xFF2A1250)
private val NeonVioletContainer = Color(0xFF3C2470)
private val OnNeonVioletContainer = Color(0xFFE8DBFF)

private val NeonAmber = Color(0xFFFFAD70)
private val OnNeonAmber = Color(0xFF3D1D00)
private val NeonAmberContainer = Color(0xFF5C3311)
private val OnNeonAmberContainer = Color(0xFFFFDCBC)

private val NeonRoseError = Color(0xFFFF97A8)
private val OnNeonRoseError = Color(0xFF400012)
private val NeonRoseErrorContainer = Color(0xFF5C1526)
private val OnNeonRoseErrorContainer = Color(0xFFFFD8DF)

/** The exact deep-black the brief calls for — every other dark surface is layered above it. */
private val VoidBackground = Color(0xFF08090B)
private val MistOnBackground = Color(0xFFEDEFF3)
private val LayerSurface = Color(0xFF111318)          // level 1: floating cards
private val MistOnSurface = Color(0xFFEDEFF3)
private val LayerSurfaceVariant = Color(0xFF1B1F27)    // level 2: nested/track/input fills
private val FogOnSurfaceVariant = Color(0xFFA7B0C0)
private val AshOutline = Color(0xFF8891A1)

val ClarityLightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = OnElectricBlue,
    primaryContainer = ElectricBlueContainer,
    onPrimaryContainer = OnElectricBlueContainer,
    secondary = VioletSecondary,
    onSecondary = OnVioletSecondary,
    secondaryContainer = VioletSecondaryContainer,
    onSecondaryContainer = OnVioletSecondaryContainer,
    tertiary = AmberTertiary,
    onTertiary = OnAmberTertiary,
    tertiaryContainer = AmberTertiaryContainer,
    onTertiaryContainer = OnAmberTertiaryContainer,
    error = RoseError,
    onError = OnRoseError,
    errorContainer = RoseErrorContainer,
    onErrorContainer = OnRoseErrorContainer,
    background = CloudBackground,
    onBackground = InkOnBackground,
    surface = PaperSurface,
    onSurface = InkOnSurface,
    surfaceVariant = MistSurfaceVariant,
    onSurfaceVariant = SlateOnSurfaceVariant,
    outline = StoneOutline,
)

val ClarityDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = OnNeonCyan,
    primaryContainer = NeonCyanContainer,
    onPrimaryContainer = OnNeonCyanContainer,
    secondary = NeonViolet,
    onSecondary = OnNeonViolet,
    secondaryContainer = NeonVioletContainer,
    onSecondaryContainer = OnNeonVioletContainer,
    tertiary = NeonAmber,
    onTertiary = OnNeonAmber,
    tertiaryContainer = NeonAmberContainer,
    onTertiaryContainer = OnNeonAmberContainer,
    error = NeonRoseError,
    onError = OnNeonRoseError,
    errorContainer = NeonRoseErrorContainer,
    onErrorContainer = OnNeonRoseErrorContainer,
    background = VoidBackground,
    onBackground = MistOnBackground,
    surface = LayerSurface,
    onSurface = MistOnSurface,
    surfaceVariant = LayerSurfaceVariant,
    onSurfaceVariant = FogOnSurfaceVariant,
    outline = AshOutline,
)

/**
 * Semantic colors Material's scheme has no slot for.
 *
 * [gradientStart]/[gradientEnd] are the two anchors for the app's signature
 * animated cyan -> violet gradient (primary buttons, hero glows, progress
 * strokes). They intentionally sit a shade deeper/more saturated than
 * [ClarityDarkColorScheme]'s own `primary`/`secondary` -- those stay light
 * and bright so they read clearly as *text/icon/line* color against
 * near-black, while the gradient anchors need enough depth that white
 * label text stays legible when painted *on top* of a filled gradient
 * across its entire span, not just at the ends.
 */
@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccessContainer: Color,
    val successContainer: Color,
    val warning: Color,
    val celebration: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
)

val LightExtendedColors = ExtendedColors(
    success = Color(0xFF1E8A5C),
    successContainer = Color(0xFFD3F5E3),
    onSuccessContainer = Color(0xFF07301D),
    warning = Color(0xFF9A6B12),
    celebration = Color(0xFFD9791F),
    gradientStart = Color(0xFF0E86C4),
    gradientEnd = Color(0xFF7A3FE0),
)

val DarkExtendedColors = ExtendedColors(
    success = Color(0xFF7EE7B8),
    successContainer = Color(0xFF0F3D2A),
    onSuccessContainer = Color(0xFFC7F5DC),
    warning = Color(0xFFFFD479),
    celebration = Color(0xFFFFC38A),
    gradientStart = Color(0xFF1FA3E8),
    gradientEnd = Color(0xFF9D5CFF),
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

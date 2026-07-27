package com.runtimelabs.clarity.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.core.designsystem.theme.MotionTokens
import com.runtimelabs.clarity.core.designsystem.theme.extended

/*
 * The "breathing glow" is this pass's signature element (see frontend-design
 * guidance: spend the one deliberate risk in a single, repeatable place).
 * It is not a decoration invented for this refresh — it is a direct
 * extension of [BreathingIndicator], which already existed as the app's
 * brand mark precisely because breathing is this app's actual coping tool.
 * Every soft bloom in the redesign (the streak ring, the SOS button, hero
 * cards) pulses on the exact same slow cadence as that indicator, so the
 * whole app quietly rehearses the same calming rhythm rather than each
 * glow being its own unrelated decoration.
 */

/** A scale + alpha pair for a slow inhale/exhale pulse. Degrades to a fixed midpoint under reduce-motion. */
@Immutable
data class BreathingPulse(val scale: Float, val alpha: Float)

@Composable
fun rememberBreathingPulse(
    reduceMotion: Boolean,
    minScale: Float = 0.92f,
    maxScale: Float = 1f,
    minAlpha: Float = 0.45f,
    maxAlpha: Float = 0.9f,
    periodMs: Int = MotionTokens.BREATH,
): BreathingPulse {
    if (reduceMotion) {
        return BreathingPulse(scale = (minScale + maxScale) / 2f, alpha = (minAlpha + maxAlpha) / 2f)
    }
    val transition = rememberInfiniteTransition(label = "breathingPulse")
    val scale by transition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMs, easing = MotionTokens.Ambient),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathingScale",
    )
    val alpha by transition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMs, easing = MotionTokens.Ambient),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathingAlpha",
    )
    return BreathingPulse(scale, alpha)
}

/**
 * A soft blurred bloom of color — the "soft bloom behind important
 * components" from the brief. Deliberately drawn as its own blurred layer
 * (rather than a background-blur/backdrop effect) so it works identically
 * on every supported API level with zero extra dependencies.
 */
@Composable
fun GlowOrb(
    color: Color,
    modifier: Modifier = Modifier,
    blurRadius: Dp = 56.dp,
) {
    Box(
        modifier = modifier
            .blur(radius = blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            .background(Brush.radialGradient(colors = listOf(color, color.copy(alpha = 0f)))),
    )
}

/** [rememberAnimatedNeonGradient] output: the shifting cyan -> violet fill used on primary buttons and hero surfaces. */
@Composable
fun rememberAnimatedNeonGradient(reduceMotion: Boolean): Brush {
    val start = MaterialTheme.extended.gradientStart
    val mid = MaterialTheme.colorScheme.primary
    val end = MaterialTheme.extended.gradientEnd
    val progress: Float
    if (reduceMotion) {
        progress = 0.5f
    } else {
        val transition = rememberInfiniteTransition(label = "neonGradientShift")
        val animated by transition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(MotionTokens.AMBIENT_DRIFT, easing = MotionTokens.Ambient),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "neonGradientShiftValue",
        )
        progress = animated
    }
    return Brush.horizontalGradient(
        colorStops = arrayOf(0f to start, progress to mid, 1f to end),
    )
}

/** Static (non-animated) version of the same gradient, for contexts that shouldn't recompose every frame (e.g. many list rows at once). */
@Composable
fun rememberStaticNeonGradient(): Brush {
    val start = MaterialTheme.extended.gradientStart
    val mid = MaterialTheme.colorScheme.primary
    val end = MaterialTheme.extended.gradientEnd
    return Brush.horizontalGradient(colorStops = arrayOf(0f to start, 0.55f to mid, 1f to end))
}

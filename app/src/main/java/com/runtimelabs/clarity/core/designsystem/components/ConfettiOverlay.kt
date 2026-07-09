package com.runtimelabs.clarity.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import com.runtimelabs.clarity.core.designsystem.theme.extended
import kotlin.random.Random

/**
 * A one-shot confetti burst: [PARTICLE_COUNT] rectangles fall from the top
 * of the composable's bounds, drifting sideways and rotating, fading out as
 * they fall. Purely decorative — hand-rolled with [Canvas] rather than a
 * new dependency, matching this app's existing pattern for bespoke motion
 * ([com.runtimelabs.clarity.feature.home.StreakRing]'s arc sweep is the
 * same "no animation library, just Canvas + Compose's own animation APIs"
 * approach).
 *
 * This is the one place in the app that deliberately DOES use a celebratory
 * animation, in contrast to the restraint documented elsewhere (the streak
 * ring's color change and the comeback-achievement rows are explicitly
 * NOT animated — ARCHITECTURE.md §21 — because those surfaces are viewed
 * many times a day and a perpetual celebration would read as gamified
 * rather than premium). A lifetime badge unlock is the opposite shape: it
 * fires at most once ever, per badge, for the life of the install — a
 * genuine one-time moment, not a repeating decoration — so the same
 * "not gamified" principle that argues against animating the streak ring
 * argues FOR marking this one differently.
 *
 * Skips entirely when [reduceMotion] is set, matching
 * [com.runtimelabs.clarity.core.util.rememberReduceMotionEnabled]'s own doc
 * comment, which already named "milestone celebrations" as motion that must
 * degrade to static when the person has disabled system animations.
 */
@Composable
fun ConfettiOverlay(
    play: Boolean,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!play || reduceMotion) return

    val particles = remember { List(PARTICLE_COUNT) { ConfettiParticle.random() } }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(DURATION_MS, easing = LinearEasing))
    }
    val colors = listOf(
        MaterialTheme.extended.celebration,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
    )
    // Animatable.value is snapshot-state-backed, so reading it directly
    // here (no `by` delegate needed) still recomposes this Canvas on every
    // animation frame, same as everywhere else in the app that drives a
    // Canvas from an animated value (see StreakRing's `progress`).
    val progressValue = progress.value

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        particles.forEachIndexed { index, particle ->
            val local = ((progressValue - particle.startDelay) / (1f - particle.startDelay)).coerceIn(0f, 1f)
            if (local <= 0f) return@forEachIndexed
            val x = (particle.startXFraction + particle.driftFraction * local) * width
            val y = local * height * particle.fallSpeedFactor
            if (y > height) return@forEachIndexed
            val alpha = (1f - local).coerceIn(0f, 1f)
            val color = colors[index % colors.size].copy(alpha = alpha)
            rotate(degrees = particle.rotationDegrees * local, pivot = Offset(x, y)) {
                drawRect(
                    color = color,
                    topLeft = Offset(x - particle.sizePx / 2f, y - particle.sizePx / 2f),
                    size = Size(particle.sizePx, particle.sizePx * 0.4f),
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val startXFraction: Float,
    val driftFraction: Float,
    val fallSpeedFactor: Float,
    val rotationDegrees: Float,
    val sizePx: Float,
    val startDelay: Float,
) {
    companion object {
        fun random(): ConfettiParticle {
            val random = Random.Default
            return ConfettiParticle(
                startXFraction = random.nextFloat(),
                driftFraction = random.nextFloat() * 0.4f - 0.2f,
                fallSpeedFactor = 0.8f + random.nextFloat() * 0.6f,
                rotationDegrees = random.nextFloat() * 720f - 360f,
                sizePx = 8f + random.nextFloat() * 10f,
                startDelay = random.nextFloat() * 0.25f,
            )
        }
    }
}

private const val PARTICLE_COUNT = 60
private const val DURATION_MS = 1600

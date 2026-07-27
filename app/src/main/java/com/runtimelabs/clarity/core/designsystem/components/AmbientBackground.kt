package com.runtimelabs.clarity.core.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.core.designsystem.theme.MotionTokens
import com.runtimelabs.clarity.core.designsystem.theme.extended
import com.runtimelabs.clarity.core.util.rememberReduceMotionEnabled
import kotlin.math.floor
import kotlin.random.Random

/*
 * The app's ambient atmosphere: two huge, soft color blooms drifting on
 * independent slow cycles, behind a scattering of near-invisible drifting
 * points. This is the "neon minimal" backdrop the whole 2026 refresh sits
 * on top of — mounted ONCE, behind the Scaffold in ClarityAppRoot and
 * behind the full-screen flows that render outside it (onboarding, the
 * recovery flow), never per-card or per-screen.
 *
 * Two restraint calls here are load-bearing for a *recovery* app
 * specifically, not just taste: alpha stays low enough that this reads as
 * atmosphere, not wallpaper — it has to disappear under a journal entry,
 * not compete with it — and motion is pure slow position drift, never a
 * pulse or strobe, so it can't read as agitating to someone on this screen
 * mid-craving. See [GlowOrb] / [MotionTokens.AMBIENT_DRIFT] for the same
 * rule applied to individual components.
 */
@Composable
fun AmbientBackdrop(modifier: Modifier = Modifier) {
    val reduceMotion = rememberReduceMotionEnabled()
    val cyan = MaterialTheme.extended.gradientStart
    val violet = MaterialTheme.extended.gradientEnd

    Box(modifier = modifier.clipToBounds()) {
        AmbientBlob(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-110).dp, y = (-90).dp)
                .size(300.dp),
            color = cyan,
            reduceMotion = reduceMotion,
            periodMs = MotionTokens.AMBIENT_DRIFT,
            driftRange = 44.dp,
        )
        AmbientBlob(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 120.dp, y = 130.dp)
                .size(340.dp),
            color = violet,
            reduceMotion = reduceMotion,
            periodMs = (MotionTokens.AMBIENT_DRIFT * 1.35f).toInt(),
            driftRange = 52.dp,
        )
        AmbientParticles(
            modifier = Modifier.matchParentSize(),
            reduceMotion = reduceMotion,
            colors = listOf(MaterialTheme.colorScheme.onBackground, cyan, violet),
        )
    }
}

@Composable
private fun AmbientBlob(
    modifier: Modifier,
    color: Color,
    reduceMotion: Boolean,
    periodMs: Int,
    driftRange: Dp,
) {
    val driftModifier = if (reduceMotion) {
        Modifier
    } else {
        val transition = rememberInfiniteTransition(label = "ambientBlobDrift")
        val drift by transition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(periodMs, easing = MotionTokens.Ambient),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "blobDrift",
        )
        Modifier.offset(x = driftRange * drift, y = driftRange * drift * 0.6f)
    }
    GlowOrb(color = color.copy(alpha = 0.16f), modifier = modifier.then(driftModifier))
}

/**
 * A sparse field of drifting points — the literal "particle background"
 * from the brief, kept deliberately faint (single-digit percent alpha) and
 * sparse (< 30 on screen) so it reads as texture on the void, not confetti.
 * Degrades to a static (non-drifting) field under reduce-motion rather than
 * disappearing entirely, same call as [GlowOrb]'s neighbors.
 */
@Composable
private fun AmbientParticles(
    modifier: Modifier = Modifier,
    reduceMotion: Boolean,
    colors: List<Color>,
) {
    val particles = remember { List(PARTICLE_COUNT) { AmbientParticle.random() } }
    val progress: Float
    if (reduceMotion) {
        progress = 0f
    } else {
        val transition = rememberInfiniteTransition(label = "ambientParticleDrift")
        val animated by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(PARTICLE_LOOP_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "particleProgress",
        )
        progress = animated
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas
        particles.forEachIndexed { index, particle ->
            val raw = particle.startYFraction - progress * particle.speedFactor
            val yFraction = raw - floor(raw)
            drawCircle(
                color = colors[index % colors.size],
                radius = particle.radiusDp.dp.toPx(),
                center = Offset(particle.xFraction * width, yFraction * height),
                alpha = particle.alpha,
            )
        }
    }
}

private data class AmbientParticle(
    val xFraction: Float,
    val startYFraction: Float,
    val speedFactor: Float,
    val radiusDp: Float,
    val alpha: Float,
) {
    companion object {
        fun random(): AmbientParticle {
            val random = Random.Default
            return AmbientParticle(
                xFraction = random.nextFloat(),
                startYFraction = random.nextFloat(),
                speedFactor = 0.5f + random.nextFloat() * 0.7f,
                radiusDp = 1.4f + random.nextFloat() * 2.4f,
                alpha = 0.05f + random.nextFloat() * 0.14f,
            )
        }
    }
}

private const val PARTICLE_COUNT = 26
private const val PARTICLE_LOOP_MS = 22000

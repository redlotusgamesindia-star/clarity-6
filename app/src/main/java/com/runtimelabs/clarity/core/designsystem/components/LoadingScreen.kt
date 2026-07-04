package com.runtimelabs.clarity.core.designsystem.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.core.util.rememberReduceMotionEnabled

/**
 * The brand loading mark: a circle that breathes at a slow, calming cadence
 * (~1.8s per phase — roughly a relaxed breath). This is the same motif the
 * Phase A breathing exercise scales up to full screen, so loading itself
 * quietly rehearses the app's core coping tool.
 *
 * Degrades to a static mark under reduce-motion.
 */
@Composable
fun BreathingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    val reduceMotion = rememberReduceMotionEnabled()
    val primary = MaterialTheme.colorScheme.primary

    val scale: Float
    val haloAlpha: Float
    if (reduceMotion) {
        scale = 1f
        haloAlpha = 0.35f
    } else {
        val transition = rememberInfiniteTransition(label = "breathing")
        val animatedScale by transition.animateFloat(
            initialValue = 0.80f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "breathScale",
        )
        val animatedAlpha by transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 0.55f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "breathAlpha",
        )
        scale = animatedScale
        haloAlpha = animatedAlpha
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = haloAlpha
                }
                .background(primary, CircleShape),
        )
        Box(
            Modifier
                .size(size * 0.42f)
                .background(primary, CircleShape),
        )
    }
}

/** Full-screen loading state. Local-DB reads are fast; this mostly guards heavier Phase C work (export, chart aggregation). */
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    val loadingLabel = stringResource(R.string.loading)
    Column(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = message ?: loadingLabel },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BreathingIndicator()
        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MaterialTheme.spacing.lg),
            )
        }
    }
}

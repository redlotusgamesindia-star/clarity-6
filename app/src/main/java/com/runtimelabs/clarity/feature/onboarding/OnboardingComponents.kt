package com.runtimelabs.clarity.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.core.designsystem.theme.MotionTokens
import com.runtimelabs.clarity.core.designsystem.theme.extended
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import kotlin.math.cos
import kotlin.math.sin

/**
 * The single answer surface for every onboarding question — one component so
 * all eleven screens feel identical in the hand. Selection is a quiet state
 * change (tinted fill, hairline -> primary border, trailing mark), never a
 * pop: calm is the brand.
 */
@Composable
fun OptionCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    multiSelect: Boolean = false,
) {
    val shape = MaterialTheme.shapes.medium
    val borderBrush = if (selected) {
        Brush.linearGradient(listOf(MaterialTheme.extended.gradientStart, MaterialTheme.extended.gradientEnd))
    } else {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.03f)))
    }
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 1.5.dp else 1.dp,
        animationSpec = tween(MotionTokens.QUICK),
        label = "optionBorderWidth",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
        },
        animationSpec = tween(MotionTokens.QUICK),
        label = "optionContainerColor",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clip(shape)
            .background(containerColor)
            .border(borderWidth, borderBrush, shape)
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (supportingText != null) {
                    Spacer(Modifier.height(MaterialTheme.spacing.hairline))
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(MaterialTheme.spacing.iconGap))
            if (multiSelect) {
                // Persistent affordance: multi-select must look checkable
                // even when unchecked.
                Icon(
                    imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null, // selection state is carried by Surface semantics
                    tint = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(24.dp),
                )
            } else {
                AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn() + scaleIn(initialScale = 0.6f),
                    exit = fadeOut(),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

/**
 * PLACEHOLDER ILLUSTRATION — replace with final art in the polish phase.
 *
 * Deterministic geometric mark (concentric arcs + a dawn-amber dot) drawn
 * from theme colors, varied per [seed] so each question feels distinct. Kept
 * abstract on purpose: it establishes the layout slot and vertical rhythm
 * real illustrations will occupy, without shipping clip-art. Purely
 * decorative -> no content description.
 */
@Composable
fun IllustrationPlaceholder(
    seed: Int,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier.size(112.dp)) {
        val outerStroke = 5.dp.toPx()
        val innerStroke = 3.5.dp.toPx()
        val outerRadius = size.minDimension / 2f - outerStroke
        val innerRadius = outerRadius * 0.62f

        // Soft backdrop disc
        drawCircle(color = primary.copy(alpha = 0.06f), radius = size.minDimension / 2f)

        // Outer arc + dawn dot riding its end
        rotate(degrees = seed * 47f) {
            drawArc(
                color = primary,
                startAngle = -90f,
                sweepAngle = 245f,
                useCenter = false,
                style = Stroke(width = outerStroke, cap = StrokeCap.Round),
                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                size = Size(outerRadius * 2f, outerRadius * 2f),
            )
            val endRad = Math.toRadians(155.0) // -90 + 245
            drawCircle(
                color = tertiary,
                radius = 5.dp.toPx(),
                center = Offset(
                    x = center.x + outerRadius * cos(endRad).toFloat(),
                    y = center.y + outerRadius * sin(endRad).toFloat(),
                ),
            )
        }

        // Counter-rotated inner arc
        rotate(degrees = seed * -31f) {
            drawArc(
                color = secondary.copy(alpha = 0.55f),
                startAngle = 40f,
                sweepAngle = 170f,
                useCenter = false,
                style = Stroke(width = innerStroke, cap = StrokeCap.Round),
                topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                size = Size(innerRadius * 2f, innerRadius * 2f),
            )
        }
    }
}

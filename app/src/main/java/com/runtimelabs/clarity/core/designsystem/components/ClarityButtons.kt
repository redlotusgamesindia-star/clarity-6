package com.runtimelabs.clarity.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.core.designsystem.theme.MotionTokens
import com.runtimelabs.clarity.core.designsystem.theme.extended
import com.runtimelabs.clarity.core.util.rememberReduceMotionEnabled

private val ButtonMinHeight = 52.dp
private val ButtonPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)

/**
 * Primary action. One per screen wherever possible — restraint is the
 * brand, unchanged from the original design doc. What changed in the 2026
 * refresh is the fill: the animated cyan → violet sweep from
 * [rememberAnimatedNeonGradient], sitting on a soft [GlowOrb] bleeding out
 * from behind it — the app's single most-repeated signature move, reused
 * nowhere else at this intensity so it keeps meaning "the one thing to
 * tap here". A light press-scale (0.97x) is the one piece of direct
 * touch-feedback motion added everywhere in this pass. [loading] swaps the
 * label for a spinner and disables input without the button changing size.
 */
@Composable
fun ClarityPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    fillWidth: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val shape = MaterialTheme.shapes.large
    val reduceMotion = rememberReduceMotionEnabled()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(MotionTokens.QUICK),
        label = "buttonPressScale",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (loading) 0f else 1f,
        animationSpec = tween(120),
        label = "buttonContentAlpha",
    )
    val gradient = rememberAnimatedNeonGradient(reduceMotion = reduceMotion)
    val fillBrush: Brush = if (enabled) gradient else SolidColor(MaterialTheme.colorScheme.surfaceVariant)

    Box {
        if (enabled) {
            GlowOrb(
                color = MaterialTheme.extended.gradientStart.copy(alpha = 0.30f),
                modifier = Modifier.matchParentSize(),
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
                .heightIn(min = ButtonMinHeight)
                .scale(pressScale)
                .alpha(if (enabled) 1f else 0.5f)
                .clip(shape)
                .background(fillBrush)
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    enabled = enabled && !loading,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(ButtonPadding),
        ) {
            CompositionLocalProvider(LocalContentColor provides Color.White) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.alpha(contentAlpha),
                    ) {
                        if (leadingIcon != null) {
                            Icon(
                                imageVector = leadingIcon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(text = text, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

/**
 * Secondary emphasis — same glass language as [ClarityCard] (translucent
 * fill, hairline gradient edge) at button geometry, rather than Material's
 * flat tonal fill.
 */
@Composable
fun ClaritySecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillWidth: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val shape = MaterialTheme.shapes.large
    val borderBrush = Brush.linearGradient(
        listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.05f)),
    )
    val fill = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = ButtonMinHeight)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(shape)
            .background(fill)
            .border(1.dp, borderBrush, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(ButtonPadding),
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    Icon(imageVector = leadingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Tertiary / inline action — unchanged in spirit, recolored onto the neon primary. */
@Composable
fun ClarityTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        modifier = modifier.heightIn(min = 44.dp), // still a comfortable touch target
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * The one floating action this app uses (add a new habit / new entry) — same
 * gradient-plus-glow language as [ClarityPrimaryButton] rather than
 * Material's stock `ExtendedFloatingActionButton`, at pill geometry since a
 * FAB reads as an action chip, not a full-width command.
 */
@Composable
fun ClarityFab(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = androidx.compose.foundation.shape.CircleShape
    val reduceMotion = rememberReduceMotionEnabled()
    val gradient = rememberAnimatedNeonGradient(reduceMotion = reduceMotion)

    Box(modifier = modifier) {
        GlowOrb(
            color = MaterialTheme.extended.gradientEnd.copy(alpha = 0.35f),
            modifier = Modifier.matchParentSize(),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .heightIn(min = 56.dp)
                .clip(shape)
                .background(gradient)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            CompositionLocalProvider(LocalContentColor provides Color.White) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

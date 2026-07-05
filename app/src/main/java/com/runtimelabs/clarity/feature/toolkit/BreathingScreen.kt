package com.runtimelabs.clarity.feature.toolkit

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.ClaritySecondaryButton
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.core.util.rememberReduceMotionEnabled

/**
 * Full-screen guided breathing. The circle inflates on the inhale, deflates
 * on the exhale, and holds still on holds — each transition animated over
 * exactly the phase's duration, so the visual IS the instruction. Under
 * reduce-motion the circle stays static and the phase label + countdown
 * carry the guidance alone.
 */
@Composable
fun BreathingScreen(
    onDone: () -> Unit,
    viewModel: BreathingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val reduceMotion = rememberReduceMotionEnabled()

    LaunchedEffect(state.finished) {
        if (state.finished) onDone()
    }
    BackHandler { viewModel.onEndSession() }

    val session = state.session
    val phase = session.currentPhase
    val targetScale = when (phase.kind) {
        BreathPhaseKind.INHALE, BreathPhaseKind.HOLD_IN -> 1f
        BreathPhaseKind.EXHALE, BreathPhaseKind.HOLD_OUT -> 0.6f
    }
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = phase.seconds * 1_000, easing = FastOutSlowInEasing),
        label = "breathScale",
    )
    val scale = if (reduceMotion) 0.8f else animatedScale

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = MaterialTheme.spacing.sm),
            ) {
                IconButton(onClick = viewModel::onEndSession) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), CircleShape),
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(phase.kind.labelRes()),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Spacer(Modifier.height(MaterialTheme.spacing.xs))
                        Text(
                            text = session.secondsRemainingInPhase.toString(),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Spacer(Modifier.height(MaterialTheme.spacing.lg))
                Text(
                    text = stringResource(R.string.breathing_cycles, session.completedCycles),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.xl))
                PatternChips(
                    selectedCode = session.pattern.code,
                    onSelected = viewModel::onPatternSelected,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.screenHorizontal)
                    .padding(bottom = MaterialTheme.spacing.lg),
            ) {
                ClaritySecondaryButton(
                    text = stringResource(R.string.breathing_end),
                    onClick = viewModel::onEndSession,
                )
            }
        }
    }
}

private fun BreathPhaseKind.labelRes(): Int = when (this) {
    BreathPhaseKind.INHALE -> R.string.breath_in
    BreathPhaseKind.HOLD_IN, BreathPhaseKind.HOLD_OUT -> R.string.breath_hold
    BreathPhaseKind.EXHALE -> R.string.breath_out
}

@Composable
private fun PatternChips(
    selectedCode: String,
    onSelected: (String) -> Unit,
) {
    val labels = mapOf(
        BreathingPatterns.CALM.code to R.string.pattern_calm,
        BreathingPatterns.BOX.code to R.string.pattern_box,
        BreathingPatterns.RELAX.code to R.string.pattern_relax,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        BreathingPatterns.ALL.forEach { pattern ->
            val selected = pattern.code == selectedCode
            Surface(
                onClick = { onSelected(pattern.code) },
                shape = CircleShape,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    width = if (selected) 1.5.dp else 1.dp,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    },
                ),
            ) {
                Text(
                    text = stringResource(labels.getValue(pattern.code)),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

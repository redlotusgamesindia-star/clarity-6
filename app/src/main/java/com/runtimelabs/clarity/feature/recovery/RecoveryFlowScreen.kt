package com.runtimelabs.clarity.feature.recovery

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.ClarityPrimaryButton
import com.runtimelabs.clarity.core.designsystem.components.ClaritySecondaryButton
import com.runtimelabs.clarity.core.designsystem.components.LoadingScreen
import com.runtimelabs.clarity.core.designsystem.theme.extended
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.core.util.rememberReduceMotionEnabled

/**
 * The five-step recovery flow. Structurally identical to OnboardingScreen on
 * purpose (progress bar, AnimatedContent slide transitions, one contextual
 * action in the bottom bar) — the person already learned this interaction
 * pattern once, during onboarding; reusing it here means the flow feels
 * familiar rather than like a new thing to figure out at a hard moment.
 */
@Composable
fun RecoveryFlowScreen(
    relapseJourneyEventId: Long,
    onDone: () -> Unit,
    onOpenBreathing: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenToolkit: () -> Unit,
    viewModel: RecoveryFlowViewModel = hiltViewModel(),
) {
    LaunchedEffect(relapseJourneyEventId) {
        viewModel.setRelapseJourneyEventId(relapseJourneyEventId)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val reduceMotion = rememberReduceMotionEnabled()

    when (val current = state) {
        RecoveryFlowUiState.Loading -> LoadingScreen()
        is RecoveryFlowUiState.Ready -> {
            LaunchedEffect(current.finished) {
                if (current.finished) onDone()
            }
            BackHandler(enabled = current.canGoBack) { viewModel.onBack() }

            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    RecoveryTopBar(state = current, onBack = viewModel::onBack)

                    AnimatedContent(
                        targetState = current.phase,
                        transitionSpec = { recoveryTransition(reduceMotion) },
                        label = "recoverySteps",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) { phase ->
                        when (phase) {
                            RecoveryFlowPhase.ACCEPT -> AcceptContent(
                                previousRunDays = current.previousRunDays,
                                bestStreakDays = current.bestStreakDays,
                            )
                            RecoveryFlowPhase.WHAT_HAPPENED -> WhatHappenedContent(
                                selected = current.draft.setbackType,
                                onSelected = viewModel::onSetbackTypeSelected,
                            )
                            RecoveryFlowPhase.FEELINGS -> FeelingsContent(
                                selected = current.draft.emotion,
                                onSelected = viewModel::onEmotionSelected,
                            )
                            RecoveryFlowPhase.TRIGGER -> TriggerContent(
                                selected = current.draft.trigger,
                                onSelected = viewModel::onTriggerSelected,
                            )
                            RecoveryFlowPhase.LEARN -> LearnContent(matchedTrigger = current.draft.trigger)
                            RecoveryFlowPhase.PLAN -> PlanContent(
                                checklist = current.checklist,
                                checkedCodes = current.checkedCodes,
                                onToggle = viewModel::onChecklistItemToggled,
                                onOpenBreathing = onOpenBreathing,
                                onOpenJournal = onOpenJournal,
                            )
                            RecoveryFlowPhase.RESTART -> RestartContent(
                                previousRunDays = current.previousRunDays,
                                onBeginNewStreak = viewModel::onBeginNewStreak,
                                onOpenBreathing = onOpenBreathing,
                                onOpenToolkit = onOpenToolkit,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RestartContent(
    previousRunDays: Int?,
    onBeginNewStreak: () -> Unit,
    onOpenBreathing: () -> Unit,
    onOpenToolkit: () -> Unit,
) {
    // Scrollable, not weight-centered like before: this screen now carries
    // more content (the "you proved" line, two shortcut buttons), and
    // Modifier.weight() inside a verticalScroll Column is a real Compose
    // anti-pattern — a scrolling container offers unbounded height, which
    // weight() has nothing sensible to divide.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.xl),
    ) {
        Spacer(Modifier.height(MaterialTheme.spacing.xxl))
        Icon(
            imageVector = Icons.Rounded.Spa,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        Text(
            text = stringResource(R.string.recovery_restart_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text(
            text = stringResource(R.string.recovery_restart_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (previousRunDays != null && previousRunDays > 0) {
            Spacer(Modifier.height(MaterialTheme.spacing.lg))
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.extended.celebration.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.recovery_restart_proof, previousRunDays),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.extended.celebration,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.md),
                )
            }
        }
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        ClarityPrimaryButton(
            text = stringResource(R.string.recovery_restart_button),
            onClick = onBeginNewStreak,
            leadingIcon = Icons.Rounded.Spa,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text(
            text = stringResource(R.string.recovery_restart_shortcuts_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        ClaritySecondaryButton(
            text = stringResource(R.string.recovery_restart_breathing_shortcut),
            onClick = onOpenBreathing,
            leadingIcon = Icons.Rounded.SelfImprovement,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        ClaritySecondaryButton(
            text = stringResource(R.string.recovery_restart_toolkit_shortcut),
            onClick = onOpenToolkit,
            leadingIcon = Icons.Rounded.Psychology,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
    }
}

private fun AnimatedContentTransitionScope<RecoveryFlowPhase>.recoveryTransition(
    reduceMotion: Boolean,
): ContentTransform {
    if (reduceMotion) {
        return fadeIn(tween(150)) togetherWith fadeOut(tween(150))
    }
    val direction = if (targetState.ordinal >= initialState.ordinal) 1 else -1
    return (
        slideInHorizontally(tween(360, easing = FastOutSlowInEasing)) { it / 4 * direction } +
            fadeIn(tween(300))
        ) togetherWith (
        slideOutHorizontally(tween(360, easing = FastOutSlowInEasing)) { -it / 4 * direction } +
            fadeOut(tween(240))
        )
}

@Composable
private fun RecoveryTopBar(
    state: RecoveryFlowUiState.Ready,
    onBack: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = MaterialTheme.spacing.sm),
    ) {
        // Plain conditional, NOT AnimatedVisibility: inside a Box nested in
        // a Row, that call resolves to the RowScope extension via the OUTER
        // receiver, which Kotlin's DslMarker rules reject — this exact bug
        // already broke a build once (OnboardingScreen's identical top bar).
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            if (state.canGoBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            }
        }
        val progress by animateFloatAsState(
            targetValue = state.progress,
            animationSpec = tween(350),
            label = "recoveryProgress",
        )
        LinearProgressIndicator(
            progress = { progress },
            strokeCap = StrokeCap.Round,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = MaterialTheme.spacing.sm),
        )
        Spacer(Modifier.size(48.dp))
    }
}

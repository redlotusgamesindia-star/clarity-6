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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import com.runtimelabs.clarity.core.designsystem.components.LoadingScreen
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.core.util.rememberReduceMotionEnabled
import com.runtimelabs.clarity.domain.model.UrgeTime

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
                            RecoveryFlowPhase.REFLECTION -> ReflectionContent(
                                draft = current.draft,
                                actions = ReflectionActions(
                                    onTriggerSelected = viewModel::onTriggerSelected,
                                    onTimeOfDaySelected = viewModel::onTimeOfDaySelected,
                                    onMoodSelected = viewModel::onMoodSelected,
                                    onLocationSelected = viewModel::onLocationSelected,
                                    onNotesChanged = viewModel::onNotesChanged,
                                ),
                            )
                            RecoveryFlowPhase.LEARN -> LearnContent(
                                matchedTrigger = current.draft.trigger,
                                matchedLateNight = current.draft.timeOfDay == UrgeTime.LATE_NIGHT,
                            )
                            RecoveryFlowPhase.PLAN -> PlanContent(
                                checklist = current.checklist,
                                checkedCodes = current.checkedCodes,
                                onToggle = viewModel::onChecklistItemToggled,
                                onOpenBreathing = onOpenBreathing,
                                onOpenJournal = onOpenJournal,
                            )
                            RecoveryFlowPhase.RESTART -> RestartContent(onBeginNewStreak = viewModel::onBeginNewStreak)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RestartContent(onBeginNewStreak: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.xl),
    ) {
        Spacer(Modifier.weight(1f))
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
        Spacer(Modifier.weight(1f))
        ClarityPrimaryButton(
            text = stringResource(R.string.recovery_restart_button),
            onClick = onBeginNewStreak,
            leadingIcon = Icons.Rounded.Spa,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
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

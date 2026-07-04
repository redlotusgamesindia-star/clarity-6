package com.runtimelabs.clarity.feature.onboarding

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.ClarityPrimaryButton
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.core.util.rememberReduceMotionEnabled

/**
 * The full onboarding flow: welcome -> eleven questions -> generation pause
 * -> plan reveal. Step navigation is ViewModel-internal state, not
 * NavController routes — onboarding is one experience with one entry and one
 * exit, and keeping it out of the nav graph means the graph never contains
 * destinations that must not be reachable later.
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val reduceMotion = rememberReduceMotionEnabled()

    BackHandler(enabled = state.canGoBack) { viewModel.onBack() }

    val actions = remember(viewModel) {
        OnboardingActions(
            onAgeSelected = viewModel::onAgeSelected,
            onGenderSelected = viewModel::onGenderSelected,
            onYearsSelected = viewModel::onYearsSelected,
            onFrequencySelected = viewModel::onFrequencySelected,
            onTriggerSelected = viewModel::onTriggerSelected,
            onUrgeTimeSelected = viewModel::onUrgeTimeSelected,
            onSleepSelected = viewModel::onSleepSelected,
            onPreviousStreakSelected = viewModel::onPreviousStreakSelected,
            onGoalSelected = viewModel::onGoalSelected,
            onReasonToggled = viewModel::onReasonToggled,
            onMotivationChanged = viewModel::onMotivationChanged,
        )
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            OnboardingTopBar(state = state, onBack = viewModel::onBack)

            AnimatedContent(
                targetState = state.screenKey(),
                transitionSpec = { onboardingTransition(reduceMotion) },
                label = "onboardingSteps",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { key ->
                when (key.phase) {
                    OnboardingPhase.WELCOME -> WelcomeContent(reduceMotion = reduceMotion)
                    OnboardingPhase.QUESTIONS -> QuestionContent(
                        question = QUESTION_ORDER[key.index],
                        draft = state.draft,
                        actions = actions,
                    )
                    OnboardingPhase.GENERATING -> GeneratingContent()
                    OnboardingPhase.PLAN -> state.plan?.let { PlanRevealContent(plan = it) }
                }
            }

            OnboardingBottomBar(
                state = state,
                onBegin = viewModel::onBegin,
                onContinue = viewModel::onContinue,
                onStartRecovery = viewModel::onStartRecovery,
            )
        }
    }
}

/**
 * Identity of the visible screen. [order] gives every screen a monotonic
 * position so the transition knows which way to slide.
 */
private data class ScreenKey(val phase: OnboardingPhase, val index: Int) {
    val order: Int get() = phase.ordinal * 100 + index
}

private fun OnboardingUiState.screenKey(): ScreenKey =
    ScreenKey(phase, if (phase == OnboardingPhase.QUESTIONS) questionIndex else 0)

/**
 * Forward: new content slides in from the right; back: from the left. A
 * quarter-width slide + fade reads as motion without being a carousel.
 * Reduce-motion collapses to a quick crossfade.
 */
private fun AnimatedContentTransitionScope<ScreenKey>.onboardingTransition(
    reduceMotion: Boolean,
): ContentTransform {
    if (reduceMotion) {
        return fadeIn(tween(150)) togetherWith fadeOut(tween(150))
    }
    val direction = if (targetState.order >= initialState.order) 1 else -1
    val easing = FastOutSlowInEasing
    return (
        slideInHorizontally(tween(360, easing = easing)) { it / 4 * direction } +
            fadeIn(tween(300))
        ) togetherWith (
        slideOutHorizontally(tween(360, easing = easing)) { -it / 4 * direction } +
            fadeOut(tween(240))
        )
}

/**
 * 56dp bar: back affordance, progress, and a mirror-image spacer so the
 * progress track stays optically centered. Progress only exists during the
 * questions — welcome and the reveal are moments, not steps.
 */
@Composable
private fun OnboardingTopBar(
    state: OnboardingUiState,
    onBack: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = MaterialTheme.spacing.sm),
    ) {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            AnimatedVisibility(visible = state.canGoBack, enter = fadeIn(), exit = fadeOut()) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            }
        }
        if (state.phase == OnboardingPhase.QUESTIONS) {
            val progress by animateFloatAsState(
                targetValue = state.progress,
                animationSpec = tween(350),
                label = "onboardingProgress",
            )
            LinearProgressIndicator(
                progress = { progress },
                strokeCap = StrokeCap.Round,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = MaterialTheme.spacing.sm),
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.size(48.dp))
    }
}

/** One primary action per phase; nothing at all while generating. */
@Composable
private fun OnboardingBottomBar(
    state: OnboardingUiState,
    onBegin: () -> Unit,
    onContinue: () -> Unit,
    onStartRecovery: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.screenHorizontal)
            .padding(top = MaterialTheme.spacing.sm, bottom = MaterialTheme.spacing.lg),
    ) {
        when (state.phase) {
            OnboardingPhase.WELCOME -> ClarityPrimaryButton(
                text = stringResource(R.string.onboarding_begin),
                onClick = onBegin,
            )
            OnboardingPhase.QUESTIONS -> ClarityPrimaryButton(
                text = stringResource(R.string.onboarding_continue),
                onClick = onContinue,
                enabled = state.canContinue,
            )
            OnboardingPhase.GENERATING -> Unit
            OnboardingPhase.PLAN -> ClarityPrimaryButton(
                text = stringResource(R.string.onboarding_start_recovery),
                onClick = onStartRecovery,
                loading = state.isSaving,
            )
        }
    }
}

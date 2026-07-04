package com.runtimelabs.clarity.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.BreathingIndicator
import com.runtimelabs.clarity.core.designsystem.components.ClarityCard
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.domain.model.AgeRange
import com.runtimelabs.clarity.domain.model.GenderIdentity
import com.runtimelabs.clarity.domain.model.MainTrigger
import com.runtimelabs.clarity.domain.model.PreviousStreak
import com.runtimelabs.clarity.domain.model.ReasonToQuit
import com.runtimelabs.clarity.domain.model.RecoveryGoal
import com.runtimelabs.clarity.domain.model.RecoveryPlan
import com.runtimelabs.clarity.domain.model.SleepSchedule
import com.runtimelabs.clarity.domain.model.UrgeTime
import com.runtimelabs.clarity.domain.model.UsageFrequency
import com.runtimelabs.clarity.domain.model.YearsAddicted
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/** Method-reference bundle so step composables take one parameter, not eleven. */
data class OnboardingActions(
    val onAgeSelected: (AgeRange) -> Unit,
    val onGenderSelected: (GenderIdentity) -> Unit,
    val onYearsSelected: (YearsAddicted) -> Unit,
    val onFrequencySelected: (UsageFrequency) -> Unit,
    val onTriggerSelected: (MainTrigger) -> Unit,
    val onUrgeTimeSelected: (UrgeTime) -> Unit,
    val onSleepSelected: (SleepSchedule) -> Unit,
    val onPreviousStreakSelected: (PreviousStreak) -> Unit,
    val onGoalSelected: (RecoveryGoal) -> Unit,
    val onReasonToggled: (ReasonToQuit) -> Unit,
    val onMotivationChanged: (Int) -> Unit,
)

// ---------------------------------------------------------------- welcome --

/**
 * Staggered three-beat entrance (mark -> title -> supporting copy), the one
 * theatrical moment in the flow. Under reduce-motion everything is simply
 * present.
 */
@Composable
fun WelcomeContent(reduceMotion: Boolean) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    @Composable
    fun beat(delayMs: Int): Float {
        val value by animateFloatAsState(
            targetValue = if (started || reduceMotion) 1f else 0f,
            animationSpec = tween(durationMillis = 500, delayMillis = if (reduceMotion) 0 else delayMs),
            label = "welcomeBeat$delayMs",
        )
        return value
    }

    val beat1 = beat(100)
    val beat2 = beat(350)
    val beat3 = beat(600)

    fun Modifier.entrance(beat: Float): Modifier = graphicsLayer {
        alpha = beat
        translationY = (1f - beat) * 24.dp.toPx()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.xl),
    ) {
        Spacer(Modifier.weight(1f))
        BreathingIndicator(size = 96.dp, modifier = Modifier.entrance(beat1))
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.entrance(beat2),
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.entrance(beat3),
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.onboarding_welcome_privacy),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.entrance(beat3),
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
    }
}

// -------------------------------------------------------------- questions --

@Composable
fun QuestionContent(
    question: OnboardingQuestion,
    draft: OnboardingDraft,
    actions: OnboardingActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
    ) {
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        IllustrationPlaceholder(
            seed = question.ordinal,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        Text(
            text = stringResource(question.titleRes()),
            style = MaterialTheme.typography.headlineMedium,
        )
        question.subtitleResOrNull()?.let { subtitle ->
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Text(
                text = stringResource(subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(MaterialTheme.spacing.lg))

        when (question) {
            OnboardingQuestion.AGE -> SingleSelectOptions(
                entries = AgeRange.entries,
                selected = draft.age,
                label = { stringResource(it.labelRes()) },
                onSelect = actions.onAgeSelected,
            )
            OnboardingQuestion.GENDER -> SingleSelectOptions(
                entries = GenderIdentity.entries,
                selected = draft.gender,
                label = { stringResource(it.labelRes()) },
                onSelect = actions.onGenderSelected,
            )
            OnboardingQuestion.YEARS_ADDICTED -> SingleSelectOptions(
                entries = YearsAddicted.entries,
                selected = draft.yearsAddicted,
                label = { stringResource(it.labelRes()) },
                onSelect = actions.onYearsSelected,
            )
            OnboardingQuestion.FREQUENCY -> SingleSelectOptions(
                entries = UsageFrequency.entries,
                selected = draft.frequency,
                label = { stringResource(it.labelRes()) },
                onSelect = actions.onFrequencySelected,
            )
            OnboardingQuestion.MAIN_TRIGGER -> SingleSelectOptions(
                entries = MainTrigger.entries,
                selected = draft.trigger,
                label = { stringResource(it.labelRes()) },
                onSelect = actions.onTriggerSelected,
            )
            OnboardingQuestion.URGE_TIME -> SingleSelectOptions(
                entries = UrgeTime.entries,
                selected = draft.urgeTime,
                label = { stringResource(it.labelRes()) },
                onSelect = actions.onUrgeTimeSelected,
            )
            OnboardingQuestion.SLEEP_SCHEDULE -> SingleSelectOptions(
                entries = SleepSchedule.entries,
                selected = draft.sleep,
                label = { stringResource(it.labelRes()) },
                onSelect = actions.onSleepSelected,
            )
            OnboardingQuestion.PREVIOUS_STREAK -> SingleSelectOptions(
                entries = PreviousStreak.entries,
                selected = draft.previousStreak,
                label = { stringResource(it.labelRes()) },
                onSelect = actions.onPreviousStreakSelected,
            )
            OnboardingQuestion.GOAL -> SingleSelectOptions(
                entries = RecoveryGoal.entries,
                selected = draft.goal,
                label = { stringResource(it.labelRes()) },
                supporting = { stringResource(it.supportingRes()) },
                onSelect = actions.onGoalSelected,
            )
            OnboardingQuestion.REASONS -> {
                ReasonToQuit.entries.forEach { reason ->
                    OptionCard(
                        text = stringResource(reason.labelRes()),
                        selected = reason in draft.reasons,
                        onClick = { actions.onReasonToggled(reason) },
                        multiSelect = true,
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                }
            }
            OnboardingQuestion.MOTIVATION -> MotivationContent(
                value = draft.motivation,
                onValueChanged = actions.onMotivationChanged,
            )
        }
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
    }
}

@Composable
private fun <T> SingleSelectOptions(
    entries: List<T>,
    selected: T?,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    supporting: (@Composable (T) -> String)? = null,
) {
    entries.forEach { entry ->
        OptionCard(
            text = label(entry),
            selected = selected == entry,
            onClick = { onSelect(entry) },
            supportingText = supporting?.invoke(entry),
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
    }
}

/** Big serif number over a stepped slider — the one non-tap answer. */
@Composable
private fun MotivationContent(
    value: Int,
    onValueChanged: (Int) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChanged(it.roundToInt()) },
            valueRange = 1f..10f,
            steps = 8,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.q_motivation_low),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.q_motivation_high),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// -------------------------------------------------------------- generating --

/**
 * Generation itself is instant; this is deliberate pacing (see ViewModel).
 * Three rotating status lines make the wait legible without a percent bar.
 */
@Composable
fun GeneratingContent() {
    val messages = remember {
        listOf(R.string.gen_msg_1, R.string.gen_msg_2, R.string.gen_msg_3)
    }
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (index < messages.lastIndex) {
            delay(720)
            index++
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.xl),
    ) {
        BreathingIndicator(size = 72.dp)
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        AnimatedContent(
            targetState = index,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "generatingMessage",
        ) { messageIndex ->
            Text(
                text = stringResource(messages[messageIndex]),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ------------------------------------------------------------- plan reveal --

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlanRevealContent(plan: RecoveryPlan) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
    ) {
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text(
            text = stringResource(R.string.plan_title),
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        Text(
            text = stringResource(R.string.plan_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.lg))

        ClarityCard {
            Text(
                text = stringResource(R.string.plan_first_milestone, plan.firstMilestoneDays),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (plan.focusAreas.isNotEmpty()) {
                Spacer(Modifier.height(MaterialTheme.spacing.md))
                Text(
                    text = stringResource(R.string.plan_focus_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                ) {
                    plan.focusAreas.forEach { area ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = stringResource(area.labelRes()),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }

        // Items arrive pre-sorted by category rank, so groupBy's LinkedHashMap
        // iterates sections in display order without re-sorting.
        plan.items.groupBy { it.category }.forEach { (category, items) ->
            Spacer(Modifier.height(MaterialTheme.spacing.lg))
            Text(
                text = stringResource(category.labelRes()).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            items.forEach { item ->
                ClarityCard {
                    Text(
                        text = stringResource(item.code.titleRes()),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.xs))
                    Text(
                        text = stringResource(item.code.descriptionRes()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
            }
        }

        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text(
            text = stringResource(R.string.plan_footnote),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xxl))
    }
}

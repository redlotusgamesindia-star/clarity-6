package com.runtimelabs.clarity.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.model.AgeRange
import com.runtimelabs.clarity.domain.model.GenderIdentity
import com.runtimelabs.clarity.domain.model.MainTrigger
import com.runtimelabs.clarity.domain.model.PreviousStreak
import com.runtimelabs.clarity.domain.model.ReasonToQuit
import com.runtimelabs.clarity.domain.model.RecoveryGoal
import com.runtimelabs.clarity.domain.model.RecoveryPlan
import com.runtimelabs.clarity.domain.model.RecoveryProfile
import com.runtimelabs.clarity.domain.model.SleepSchedule
import com.runtimelabs.clarity.domain.model.UrgeTime
import com.runtimelabs.clarity.domain.model.UsageFrequency
import com.runtimelabs.clarity.domain.model.YearsAddicted
import com.runtimelabs.clarity.domain.plan.RecoveryPlanGenerator
import com.runtimelabs.clarity.domain.repository.RecoveryProfileRepository
import com.runtimelabs.clarity.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The eleven questions, in narrative order: you -> your pattern -> your commitment. */
enum class OnboardingQuestion {
    AGE,
    GENDER,
    YEARS_ADDICTED,
    FREQUENCY,
    MAIN_TRIGGER,
    URGE_TIME,
    SLEEP_SCHEDULE,
    PREVIOUS_STREAK,
    GOAL,
    REASONS,
    MOTIVATION,
}

val QUESTION_ORDER: List<OnboardingQuestion> = OnboardingQuestion.entries

/**
 * In-progress answers. Nullable until answered; motivation pre-set to the
 * midpoint so the slider starts neutral. Lost on process death — acceptable
 * for a two-minute flow (documented trade-off, ARCHITECTURE.md §16).
 */
data class OnboardingDraft(
    val age: AgeRange? = null,
    val gender: GenderIdentity? = null,
    val yearsAddicted: YearsAddicted? = null,
    val frequency: UsageFrequency? = null,
    val trigger: MainTrigger? = null,
    val urgeTime: UrgeTime? = null,
    val sleep: SleepSchedule? = null,
    val previousStreak: PreviousStreak? = null,
    val goal: RecoveryGoal? = null,
    val reasons: Set<ReasonToQuit> = emptySet(),
    val motivation: Int = 5,
)

enum class OnboardingPhase { WELCOME, QUESTIONS, GENERATING, PLAN }

data class OnboardingUiState(
    val phase: OnboardingPhase = OnboardingPhase.WELCOME,
    val questionIndex: Int = 0,
    val draft: OnboardingDraft = OnboardingDraft(),
    val plan: RecoveryPlan? = null,
    val isSaving: Boolean = false,
) {
    val currentQuestion: OnboardingQuestion get() = QUESTION_ORDER[questionIndex]

    val progress: Float get() = (questionIndex + 1f) / QUESTION_ORDER.size

    val canGoBack: Boolean get() = phase == OnboardingPhase.QUESTIONS || phase == OnboardingPhase.PLAN

    val canContinue: Boolean
        get() = phase != OnboardingPhase.QUESTIONS || when (currentQuestion) {
            OnboardingQuestion.AGE -> draft.age != null
            OnboardingQuestion.GENDER -> draft.gender != null
            OnboardingQuestion.YEARS_ADDICTED -> draft.yearsAddicted != null
            OnboardingQuestion.FREQUENCY -> draft.frequency != null
            OnboardingQuestion.MAIN_TRIGGER -> draft.trigger != null
            OnboardingQuestion.URGE_TIME -> draft.urgeTime != null
            OnboardingQuestion.SLEEP_SCHEDULE -> draft.sleep != null
            OnboardingQuestion.PREVIOUS_STREAK -> draft.previousStreak != null
            OnboardingQuestion.GOAL -> draft.goal != null
            OnboardingQuestion.REASONS -> draft.reasons.isNotEmpty()
            OnboardingQuestion.MOTIVATION -> true
        }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val recoveryProfileRepository: RecoveryProfileRepository,
    private val planGenerator: RecoveryPlanGenerator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun onBegin() {
        _uiState.update { it.copy(phase = OnboardingPhase.QUESTIONS, questionIndex = 0) }
    }

    fun onBack() {
        _uiState.update { state ->
            when (state.phase) {
                OnboardingPhase.QUESTIONS ->
                    if (state.questionIndex == 0) {
                        state.copy(phase = OnboardingPhase.WELCOME)
                    } else {
                        state.copy(questionIndex = state.questionIndex - 1)
                    }
                // Back from the plan returns to the last question so answers
                // can be tweaked; the plan regenerates on Continue.
                OnboardingPhase.PLAN -> state.copy(
                    phase = OnboardingPhase.QUESTIONS,
                    questionIndex = QUESTION_ORDER.lastIndex,
                    plan = null,
                )
                else -> state
            }
        }
    }

    fun onContinue() {
        val state = _uiState.value
        if (state.phase != OnboardingPhase.QUESTIONS || !state.canContinue) return
        if (state.questionIndex < QUESTION_ORDER.lastIndex) {
            _uiState.update { it.copy(questionIndex = it.questionIndex + 1) }
        } else {
            generatePlan()
        }
    }

    private fun generatePlan() {
        val profile = _uiState.value.draft.toProfile(System.currentTimeMillis()) ?: return
        _uiState.update { it.copy(phase = OnboardingPhase.GENERATING) }
        viewModelScope.launch {
            val plan = planGenerator.generate(profile)
            // Generation is instantaneous; the pause is deliberate pacing so
            // the reveal has weight. Honest theater — nothing is "computing".
            delay(GENERATION_PACING_MS)
            _uiState.update { it.copy(phase = OnboardingPhase.PLAN, plan = plan) }
        }
    }

    fun onStartRecovery() {
        val state = _uiState.value
        if (state.isSaving) return
        val plan = state.plan ?: return
        val profile = state.draft.toProfile(System.currentTimeMillis()) ?: return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            // Order matters: persist the data, then flip the flag the app
            // shell observes — the shell swap is the success signal.
            recoveryProfileRepository.saveProfileAndPlan(profile, plan)
            settingsRepository.setOnboardingCompleted(true)
        }
    }

    // ---- answer setters ----

    fun onAgeSelected(value: AgeRange) = updateDraft { it.copy(age = value) }
    fun onGenderSelected(value: GenderIdentity) = updateDraft { it.copy(gender = value) }
    fun onYearsSelected(value: YearsAddicted) = updateDraft { it.copy(yearsAddicted = value) }
    fun onFrequencySelected(value: UsageFrequency) = updateDraft { it.copy(frequency = value) }
    fun onTriggerSelected(value: MainTrigger) = updateDraft { it.copy(trigger = value) }
    fun onUrgeTimeSelected(value: UrgeTime) = updateDraft { it.copy(urgeTime = value) }
    fun onSleepSelected(value: SleepSchedule) = updateDraft { it.copy(sleep = value) }
    fun onPreviousStreakSelected(value: PreviousStreak) = updateDraft { it.copy(previousStreak = value) }
    fun onGoalSelected(value: RecoveryGoal) = updateDraft { it.copy(goal = value) }

    fun onReasonToggled(reason: ReasonToQuit) = updateDraft {
        it.copy(reasons = if (reason in it.reasons) it.reasons - reason else it.reasons + reason)
    }

    fun onMotivationChanged(value: Int) = updateDraft { it.copy(motivation = value.coerceIn(1, 10)) }

    private inline fun updateDraft(block: (OnboardingDraft) -> OnboardingDraft) {
        _uiState.update { it.copy(draft = block(it.draft)) }
    }

    private companion object {
        const val GENERATION_PACING_MS = 2_200L
    }
}

/** Null if any required answer is missing (gated by [OnboardingUiState.canContinue]). */
private fun OnboardingDraft.toProfile(createdAtEpochMillis: Long): RecoveryProfile? {
    return RecoveryProfile(
        ageRange = age ?: return null,
        gender = gender ?: return null,
        yearsAddicted = yearsAddicted ?: return null,
        frequency = frequency ?: return null,
        mainTrigger = trigger ?: return null,
        goal = goal ?: return null,
        motivationLevel = motivation,
        // Sorted for determinism: the same selections always produce an
        // identical profile object regardless of tap order.
        reasonsToQuit = reasons.sortedBy { it.ordinal },
        previousStreak = previousStreak ?: return null,
        strongestUrgeTime = urgeTime ?: return null,
        sleepSchedule = sleep ?: return null,
        createdAtEpochMillis = createdAtEpochMillis,
    )
}

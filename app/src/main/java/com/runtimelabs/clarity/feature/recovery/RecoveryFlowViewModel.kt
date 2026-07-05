package com.runtimelabs.clarity.feature.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.model.JourneyEventType
import com.runtimelabs.clarity.domain.model.MainTrigger
import com.runtimelabs.clarity.domain.model.MoodLevel
import com.runtimelabs.clarity.domain.model.RelapseLocation
import com.runtimelabs.clarity.domain.model.RelapseReflection
import com.runtimelabs.clarity.domain.model.UrgeTime
import com.runtimelabs.clarity.domain.recovery.RecoveryChecklistGenerator
import com.runtimelabs.clarity.domain.recovery.RecoveryChecklistItem
import com.runtimelabs.clarity.domain.recovery.RecoveryChecklistItemCode
import com.runtimelabs.clarity.domain.repository.JourneyRepository
import com.runtimelabs.clarity.domain.repository.RecoveryProfileRepository
import com.runtimelabs.clarity.domain.repository.RelapseReflectionRepository
import com.runtimelabs.clarity.domain.streak.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Accept -> Reflection -> Learn -> Plan -> Restart. Same architectural
 * choice as onboarding and the exact same reasons: one experience, one
 * entry, one exit, so step navigation is VM-internal state rather than
 * NavController routes (ARCHITECTURE.md §16, extended §22).
 *
 * The relapse JourneyEvent is already recorded by the time this VM is
 * created (HomeViewModel does that the moment the person confirms, before
 * navigating here) — this flow is entirely about support, so its own
 * failure to complete (closing the app mid-flow) must never leave the
 * streak in a half-reset state. Only the OPTIONAL reflection and the
 * ephemeral checklist live here.
 */
enum class RecoveryFlowPhase { ACCEPT, REFLECTION, LEARN, PLAN, RESTART }

data class RecoveryFlowDraft(
    val trigger: MainTrigger? = null,
    val timeOfDay: UrgeTime? = null,
    val mood: MoodLevel? = null,
    val location: RelapseLocation? = null,
    val notes: String = "",
)

sealed interface RecoveryFlowUiState {
    data object Loading : RecoveryFlowUiState

    data class Ready(
        val phase: RecoveryFlowPhase,
        val draft: RecoveryFlowDraft,
        /** The run that just ended; null the very first time this ever happens. */
        val previousRunDays: Int?,
        val bestStreakDays: Int,
        val checklist: List<RecoveryChecklistItem>,
        /** Ephemeral — not persisted. This is a one-time show of momentum, not a habit tracker. */
        val checkedCodes: Set<RecoveryChecklistItemCode>,
        val finished: Boolean,
    ) : RecoveryFlowUiState {
        val progress: Float get() = (phase.ordinal + 1f) / RecoveryFlowPhase.entries.size
        val canGoBack: Boolean get() = phase.ordinal > 0
    }
}

@HiltViewModel
class RecoveryFlowViewModel @Inject constructor(
    private val profileRepository: RecoveryProfileRepository,
    private val journeyRepository: JourneyRepository,
    private val relapseReflectionRepository: RelapseReflectionRepository,
    private val streakCalculator: StreakCalculator,
    private val checklistGenerator: RecoveryChecklistGenerator,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecoveryFlowUiState>(RecoveryFlowUiState.Loading)
    val uiState = _uiState.asStateFlow()

    /** The journey event id the just-confirmed relapse was recorded under. */
    private var relapseJourneyEventId: Long = 0

    init {
        viewModelScope.launch {
            val profile = profileRepository.profile.first()
            val relapseDays = journeyRepository.observeEventDays(JourneyEventType.RELAPSE).first()
            val today = LocalDate.now().toEpochDay()
            val startDay = profile?.createdAtEpochMillis
                ?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay() }
                ?: today
            val streak = streakCalculator.compute(startDay, relapseDays, today)

            _uiState.value = RecoveryFlowUiState.Ready(
                phase = RecoveryFlowPhase.ACCEPT,
                draft = RecoveryFlowDraft(),
                previousRunDays = streak.previousRunDays,
                bestStreakDays = streak.longestDays,
                checklist = emptyList(),
                checkedCodes = emptySet(),
                finished = false,
            )
        }
    }

    /** Set by [com.runtimelabs.clarity.feature.home.HomeViewModel] before navigating here. */
    fun setRelapseJourneyEventId(id: Long) {
        relapseJourneyEventId = id
    }

    fun onContinue() {
        updateReady { state ->
            when (state.phase) {
                RecoveryFlowPhase.ACCEPT -> state.copy(phase = RecoveryFlowPhase.REFLECTION)
                RecoveryFlowPhase.REFLECTION -> {
                    saveReflectionIfAnyFieldFilled(state.draft)
                    state.copy(phase = RecoveryFlowPhase.LEARN)
                }
                RecoveryFlowPhase.LEARN -> state.copy(
                    phase = RecoveryFlowPhase.PLAN,
                    checklist = checklistGenerator.generate(state.draft.trigger, state.draft.timeOfDay),
                )
                RecoveryFlowPhase.PLAN -> state.copy(phase = RecoveryFlowPhase.RESTART)
                RecoveryFlowPhase.RESTART -> state
            }
        }
    }

    fun onBack() {
        updateReady { state ->
            if (state.phase.ordinal == 0) state else {
                val previous = RecoveryFlowPhase.entries[state.phase.ordinal - 1]
                state.copy(phase = previous)
            }
        }
    }

    fun onTriggerSelected(value: MainTrigger) = updateDraft { it.copy(trigger = value) }
    fun onTimeOfDaySelected(value: UrgeTime) = updateDraft { it.copy(timeOfDay = value) }
    fun onMoodSelected(value: MoodLevel) = updateDraft { it.copy(mood = value) }
    fun onLocationSelected(value: RelapseLocation) = updateDraft { it.copy(location = value) }
    fun onNotesChanged(value: String) = updateDraft { it.copy(notes = value) }

    fun onChecklistItemToggled(code: RecoveryChecklistItemCode) {
        updateReady { state ->
            state.copy(
                checkedCodes = if (code in state.checkedCodes) {
                    state.checkedCodes - code
                } else {
                    state.checkedCodes + code
                },
            )
        }
    }

    /** The large "Begin new streak" button. Just closes the flow — the streak was already reset at confirm time. */
    fun onBeginNewStreak() {
        updateReady { it.copy(finished = true) }
    }

    private fun saveReflectionIfAnyFieldFilled(draft: RecoveryFlowDraft) {
        val hasAnything = draft.trigger != null || draft.timeOfDay != null ||
            draft.mood != null || draft.location != null || draft.notes.isNotBlank()
        if (!hasAnything) return // nothing offered — nothing to save, no empty row created
        viewModelScope.launch {
            relapseReflectionRepository.save(
                RelapseReflection(
                    id = RelapseReflection.NEW_ID,
                    journeyEventId = relapseJourneyEventId,
                    epochDay = LocalDate.now().toEpochDay(),
                    createdAtEpochMillis = System.currentTimeMillis(),
                    trigger = draft.trigger,
                    timeOfDay = draft.timeOfDay,
                    mood = draft.mood,
                    location = draft.location,
                    notes = draft.notes.trim(),
                ),
            )
        }
    }

    private inline fun updateReady(block: (RecoveryFlowUiState.Ready) -> RecoveryFlowUiState.Ready) {
        _uiState.update { state -> if (state is RecoveryFlowUiState.Ready) block(state) else state }
    }

    private inline fun updateDraft(block: (RecoveryFlowDraft) -> RecoveryFlowDraft) {
        updateReady { it.copy(draft = block(it.draft)) }
    }
}

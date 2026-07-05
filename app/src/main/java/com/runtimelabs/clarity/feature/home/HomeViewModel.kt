package com.runtimelabs.clarity.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.model.DailyCheckIn
import com.runtimelabs.clarity.domain.model.JourneyEvent
import com.runtimelabs.clarity.domain.model.JourneyEventType
import com.runtimelabs.clarity.domain.model.MoodLevel
import com.runtimelabs.clarity.domain.model.StreakSnapshot
import com.runtimelabs.clarity.domain.recovery.RecoveryMotivationCode
import com.runtimelabs.clarity.domain.recovery.RecoveryMotivationMessages
import com.runtimelabs.clarity.domain.repository.CheckInRepository
import com.runtimelabs.clarity.domain.repository.JourneyRepository
import com.runtimelabs.clarity.domain.repository.RecoveryProfileRepository
import com.runtimelabs.clarity.domain.repository.WidgetSyncRepository
import com.runtimelabs.clarity.domain.streak.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Ready(
        val streak: StreakSnapshot,
        val milestoneDays: Int,
        val todayEpochDay: Long,
        val todayCheckIn: DailyCheckIn?,
        /** Check-ins from the last 7 days (inclusive), ascending. */
        val weekCheckIns: List<DailyCheckIn>,
        /** Non-null while the check-in sheet is open. */
        val checkInSheet: CheckInSheetState?,
        /** True while the relapse confirmation dialog is showing. */
        val showRelapseConfirm: Boolean,
        /** Set the moment a relapse is confirmed; the screen consumes this to navigate, then clears it. */
        val pendingRecoveryFlowEventId: Long?,
        /** Shown only while [StreakSnapshot.isRebuilding] — the Motivation Engine's line for today. */
        val motivationMessage: RecoveryMotivationCode?,
    ) : HomeUiState
}

data class CheckInSheetState(
    val mood: MoodLevel? = null,
    val urgeLevel: Int = 0,
    val isSaving: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    profileRepository: RecoveryProfileRepository,
    private val journeyRepository: JourneyRepository,
    private val checkInRepository: CheckInRepository,
    private val streakCalculator: StreakCalculator,
    private val widgetSyncRepository: WidgetSyncRepository,
) : ViewModel() {

    // Captured at init. A session held open across midnight shows the old
    // frame until the screen is re-entered — documented v1 trade-off (§17).
    // Writes are exempt: onSaveCheckIn stamps its own day at save time.
    private val todayEpochDay: Long = LocalDate.now().toEpochDay()

    private val sheetState = MutableStateFlow<CheckInSheetState?>(null)
    private val showRelapseConfirm = MutableStateFlow(false)
    private val pendingRecoveryFlowEventId = MutableStateFlow<Long?>(null)

    // combine() tops out at 5 named flow arguments (kotlinx.coroutines has no
    // 6-flow overload — confirmed against the maintainers' own tracker, not
    // assumed). Folding the three small UI-only flows into one Triple here
    // keeps the outer combine below at exactly 5, same trick JourneyViewModel
    // already uses for its streak inputs.
    private val dialogAndSheetState = combine(
        showRelapseConfirm,
        pendingRecoveryFlowEventId,
        sheetState,
        ::Triple,
    )

    val uiState: StateFlow<HomeUiState> = combine(
        profileRepository.profile,
        profileRepository.plan,
        journeyRepository.observeEventDays(JourneyEventType.RELAPSE),
        checkInRepository.observeSince(todayEpochDay - 6),
        dialogAndSheetState,
    ) { profile, plan, relapseDays, weekCheckIns, (showConfirm, pendingEventId, sheet) ->
        val startEpochDay = profile?.createdAtEpochMillis
            ?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay() }
            ?: todayEpochDay // defensive: no profile should be impossible post-onboarding
        val streak = streakCalculator.compute(
            recoveryStartEpochDay = startEpochDay,
            relapseEpochDays = relapseDays,
            todayEpochDay = todayEpochDay,
        )
        HomeUiState.Ready(
            streak = streak,
            milestoneDays = plan?.firstMilestoneDays ?: DEFAULT_MILESTONE_DAYS,
            todayEpochDay = todayEpochDay,
            todayCheckIn = weekCheckIns.firstOrNull { it.epochDay == todayEpochDay },
            weekCheckIns = weekCheckIns,
            checkInSheet = sheet,
            showRelapseConfirm = showConfirm,
            pendingRecoveryFlowEventId = pendingEventId,
            motivationMessage = if (streak.isRebuilding) {
                RecoveryMotivationMessages.forDay(streak.currentDays)
            } else {
                null
            },
        ) as HomeUiState
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading,
    )

    fun onOpenCheckIn() {
        val ready = uiState.value as? HomeUiState.Ready ?: return
        val existing = ready.todayCheckIn
        sheetState.value = CheckInSheetState(
            mood = existing?.mood,
            urgeLevel = existing?.urgeLevel ?: 0,
        )
    }

    fun onDismissCheckIn() {
        sheetState.value = null
    }

    fun onMoodSelected(mood: MoodLevel) {
        sheetState.value = sheetState.value?.copy(mood = mood)
    }

    fun onUrgeChanged(level: Int) {
        sheetState.value = sheetState.value?.copy(urgeLevel = level.coerceIn(0, 10))
    }

    fun onSaveCheckIn() {
        val sheet = sheetState.value ?: return
        val mood = sheet.mood ?: return
        if (sheet.isSaving) return
        sheetState.value = sheet.copy(isSaving = true)
        viewModelScope.launch {
            checkInRepository.upsert(
                DailyCheckIn(
                    epochDay = LocalDate.now().toEpochDay(), // stamped now, not at VM init
                    mood = mood,
                    urgeLevel = sheet.urgeLevel,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
            sheetState.value = null
        }
    }

    fun onRelapseButtonTapped() {
        showRelapseConfirm.value = true
    }

    fun onRelapseDialogDismissed() {
        showRelapseConfirm.value = false
    }

    /**
     * The one action that matters most in this whole screen: record the
     * fact immediately (never gated behind a multi-step flow completing),
     * then hand the new event's id to the UI to open the Recovery Flow.
     * Order mirrors onboarding's completion: persist first, let the
     * observed state drive what happens next.
     */
    fun onRelapseConfirmed() {
        showRelapseConfirm.value = false
        viewModelScope.launch {
            val eventId = journeyRepository.record(
                JourneyEvent(
                    id = 0,
                    type = JourneyEventType.RELAPSE,
                    occurredAtEpochMillis = System.currentTimeMillis(),
                    epochDay = LocalDate.now().toEpochDay(),
                ),
            )
            widgetSyncRepository.refresh()
            pendingRecoveryFlowEventId.value = eventId
        }
    }

    /** Called once the screen has navigated to the Recovery Flow, so it doesn't fire again on recomposition. */
    fun onRecoveryFlowNavigated() {
        pendingRecoveryFlowEventId.value = null
    }

    private companion object {
        const val DEFAULT_MILESTONE_DAYS = 7
    }
}

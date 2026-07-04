package com.runtimelabs.clarity.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.model.DailyCheckIn
import com.runtimelabs.clarity.domain.model.JourneyEventType
import com.runtimelabs.clarity.domain.model.MoodLevel
import com.runtimelabs.clarity.domain.model.StreakSnapshot
import com.runtimelabs.clarity.domain.repository.CheckInRepository
import com.runtimelabs.clarity.domain.repository.JourneyRepository
import com.runtimelabs.clarity.domain.repository.RecoveryProfileRepository
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
    journeyRepository: JourneyRepository,
    private val checkInRepository: CheckInRepository,
    private val streakCalculator: StreakCalculator,
) : ViewModel() {

    // Captured at init. A session held open across midnight shows the old
    // frame until the screen is re-entered — documented v1 trade-off (§17).
    // Writes are exempt: onSaveCheckIn stamps its own day at save time.
    private val todayEpochDay: Long = LocalDate.now().toEpochDay()

    private val sheetState = MutableStateFlow<CheckInSheetState?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        profileRepository.profile,
        profileRepository.plan,
        journeyRepository.observeEventDays(JourneyEventType.RELAPSE),
        checkInRepository.observeSince(todayEpochDay - 6),
        sheetState,
    ) { profile, plan, relapseDays, weekCheckIns, sheet ->
        val startEpochDay = profile?.createdAtEpochMillis
            ?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay() }
            ?: todayEpochDay // defensive: no profile should be impossible post-onboarding
        HomeUiState.Ready(
            streak = streakCalculator.compute(
                recoveryStartEpochDay = startEpochDay,
                relapseEpochDays = relapseDays,
                todayEpochDay = todayEpochDay,
            ),
            milestoneDays = plan?.firstMilestoneDays ?: DEFAULT_MILESTONE_DAYS,
            todayEpochDay = todayEpochDay,
            todayCheckIn = weekCheckIns.firstOrNull { it.epochDay == todayEpochDay },
            weekCheckIns = weekCheckIns,
            checkInSheet = sheet,
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

    private companion object {
        const val DEFAULT_MILESTONE_DAYS = 7
    }
}

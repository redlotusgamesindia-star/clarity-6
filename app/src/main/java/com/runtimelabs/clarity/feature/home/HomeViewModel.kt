package com.runtimelabs.clarity.feature.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.badge.Badge
import com.runtimelabs.clarity.domain.model.DailyCheckIn
import com.runtimelabs.clarity.domain.model.JourneyEvent
import com.runtimelabs.clarity.domain.model.JourneyEventType
import com.runtimelabs.clarity.domain.model.MoodLevel
import com.runtimelabs.clarity.domain.model.StreakSnapshot
import com.runtimelabs.clarity.domain.recovery.RecoveryMotivationCode
import com.runtimelabs.clarity.domain.recovery.RecoveryMotivationMessages
import com.runtimelabs.clarity.domain.repository.BadgeRepository
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

    /** @Immutable: the `List<DailyCheckIn>`/`List<Badge>` fields are otherwise conservatively-unstable — see JournalUiState's fuller rationale. */
    @Immutable
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
        /**
         * Badges genuinely earned just now — by a check-in, a relapse (and
         * the new recovery run it starts), or simply by opening the app
         * for the first time since qualifying for one retroactively. The
         * screen shows the unlock celebration for these, then consumes
         * this via [onAchievementCelebrationDismissed]. Empty in the
         * overwhelmingly common case where nothing new was unlocked.
         */
        val newlyUnlockedBadges: List<Badge>,
    ) : HomeUiState
}

data class CheckInSheetState(
    val mood: MoodLevel? = null,
    val urgeLevel: Int = 0,
    val isSaving: Boolean = false,
)

/** The small UI-only signals folded together to stay under combine()'s 5-flow ceiling — see below. */
private data class HomeTransientState(
    val showRelapseConfirm: Boolean,
    val pendingRecoveryFlowEventId: Long?,
    val checkInSheet: CheckInSheetState?,
    val newlyUnlockedBadges: List<Badge>,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    profileRepository: RecoveryProfileRepository,
    private val journeyRepository: JourneyRepository,
    private val checkInRepository: CheckInRepository,
    private val streakCalculator: StreakCalculator,
    private val widgetSyncRepository: WidgetSyncRepository,
    private val badgeRepository: BadgeRepository,
) : ViewModel() {

    // Captured at init. A session held open across midnight shows the old
    // frame until the screen is re-entered — documented v1 trade-off (§17).
    // Writes are exempt: onSaveCheckIn stamps its own day at save time.
    private val todayEpochDay: Long = LocalDate.now().toEpochDay()

    private val sheetState = MutableStateFlow<CheckInSheetState?>(null)
    private val showRelapseConfirm = MutableStateFlow(false)
    private val pendingRecoveryFlowEventId = MutableStateFlow<Long?>(null)
    private val newlyUnlockedBadges = MutableStateFlow<List<Badge>>(emptyList())

    // combine() tops out at 5 named flow arguments (kotlinx.coroutines has no
    // 6-flow overload — confirmed against the maintainers' own tracker, not
    // assumed). Folding the four small UI-only flows into one data class
    // here keeps the outer combine below at exactly 5, same trick
    // JourneyViewModel already uses for its streak inputs (§24). This grew
    // from a Triple to a named data class when the badge signal joined it —
    // a fourth positional field on a Triple has no fourth slot to grow
    // into, and a named type reads better than nesting Pairs.
    private val transientState = combine(
        showRelapseConfirm,
        pendingRecoveryFlowEventId,
        sheetState,
        newlyUnlockedBadges,
        ::HomeTransientState,
    )

    val uiState: StateFlow<HomeUiState> = combine(
        profileRepository.profile,
        profileRepository.plan,
        journeyRepository.observeEventDays(JourneyEventType.RELAPSE),
        checkInRepository.observeSince(todayEpochDay - 6),
        transientState,
    ) { profile, plan, relapseDays, weekCheckIns, transient ->
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
            checkInSheet = transient.checkInSheet,
            showRelapseConfirm = transient.showRelapseConfirm,
            pendingRecoveryFlowEventId = transient.pendingRecoveryFlowEventId,
            motivationMessage = if (streak.isRebuilding) {
                RecoveryMotivationMessages.forDay(streak.currentDays)
            } else {
                null
            },
            newlyUnlockedBadges = transient.newlyUnlockedBadges,
        ) as HomeUiState
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading,
    )

    init {
        // Cold-start backfill: re-check every badge against current state
        // once per Home visit. Deliberately not silent — see
        // AchievementUnlockOverlay's doc comment for why celebrating a
        // badge that turns out to already be earned (e.g. right after this
        // feature ships to someone with an existing 40-day streak) is
        // treated as a genuine, honest one-time moment rather than
        // suppressed the way ARCHITECTURE.md §21 suppresses REPEATING
        // celebrations elsewhere — this one only ever fires once per badge,
        // for the life of the install.
        runEvaluation()
    }

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
            runEvaluation() // catches Morning Check-in and any streak badge the new day crossed
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
            // A relapse is the start of a new recovery run in this app's own
            // vocabulary (§22) — First/Five Recoveries can genuinely unlock
            // right here, the same moment totalRelapses actually changes.
            runEvaluation()
        }
    }

    /** Called once the screen has navigated to the Recovery Flow, so it doesn't fire again on recomposition. */
    fun onRecoveryFlowNavigated() {
        pendingRecoveryFlowEventId.value = null
    }

    /** Consumes the current celebration (or the current badge within a multi-badge sequence — see the overlay). */
    fun onAchievementCelebrationDismissed() {
        newlyUnlockedBadges.value = emptyList()
    }

    private fun runEvaluation() {
        viewModelScope.launch {
            val newlyUnlocked = badgeRepository.evaluateAndUnlock()
            if (newlyUnlocked.isNotEmpty()) {
                // Additive: a badge unlocked by a later call (e.g. the
                // relapse-confirm evaluation firing before the check-in
                // evaluation from this same visit has been dismissed)
                // extends the queue rather than clobbering it.
                newlyUnlockedBadges.value = newlyUnlockedBadges.value + newlyUnlocked
            }
        }
    }

    private companion object {
        const val DEFAULT_MILESTONE_DAYS = 7
    }
}

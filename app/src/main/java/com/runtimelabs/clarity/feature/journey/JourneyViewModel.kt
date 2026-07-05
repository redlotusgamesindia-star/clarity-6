package com.runtimelabs.clarity.feature.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.habit.DayStat
import com.runtimelabs.clarity.domain.habit.HabitStatsCalculator
import com.runtimelabs.clarity.domain.insight.Insight
import com.runtimelabs.clarity.domain.insight.InsightGenerator
import com.runtimelabs.clarity.domain.model.Habit
import com.runtimelabs.clarity.domain.model.JourneyEventType
import com.runtimelabs.clarity.domain.model.isScheduledOn
import com.runtimelabs.clarity.domain.recovery.ComebackAchievement
import com.runtimelabs.clarity.domain.recovery.RecoveryScore
import com.runtimelabs.clarity.domain.recovery.RecoveryScoreCalculator
import com.runtimelabs.clarity.domain.recovery.unlockedComebackAchievements
import com.runtimelabs.clarity.domain.repository.CheckInRepository
import com.runtimelabs.clarity.domain.repository.HabitRepository
import com.runtimelabs.clarity.domain.repository.JourneyRepository
import com.runtimelabs.clarity.domain.repository.RecoveryProfileRepository
import com.runtimelabs.clarity.domain.streak.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Week-strip dot for one habit-day. OPEN is neutral by design — no shame red. */
enum class HabitDayDot { DONE, OPEN, OFF }

data class HabitWithStatus(
    val habit: Habit,
    val doneToday: Boolean,
    /** Last 7 days, oldest first. */
    val weekDots: List<HabitDayDot>,
)

sealed interface JourneyUiState {
    data object Loading : JourneyUiState

    data class Ready(
        val todayEpochDay: Long,
        val todaysHabits: List<HabitWithStatus>,
        val otherHabits: List<Habit>,
        val weekDays: List<DayStat>,
        val weekCompleted: Int,
        val weekScheduled: Int,
        val insights: List<Insight>,
        val recoveryScore: RecoveryScore,
        /** Empty until the first relapse — the section is hidden entirely until then. */
        val comebackAchievements: List<ComebackAchievement>,
    ) : JourneyUiState {
        val hasHabits: Boolean get() = todaysHabits.isNotEmpty() || otherHabits.isNotEmpty()
    }
}

@HiltViewModel
class JourneyViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    checkInRepository: CheckInRepository,
    profileRepository: RecoveryProfileRepository,
    journeyRepository: JourneyRepository,
    streakCalculator: StreakCalculator,
    statsCalculator: HabitStatsCalculator,
    insightGenerator: InsightGenerator,
    recoveryScoreCalculator: RecoveryScoreCalculator,
) : ViewModel() {

    private val todayEpochDay: Long = LocalDate.now().toEpochDay()
    private val zone: ZoneId = ZoneId.systemDefault()

    // Streak folded into one flow so the main combine stays within five inputs.
    private val streakFlow = combine(
        profileRepository.profile,
        journeyRepository.observeEventDays(JourneyEventType.RELAPSE),
    ) { profile, relapseDays ->
        val startDay = profile?.createdAtEpochMillis
            ?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate().toEpochDay() }
            ?: todayEpochDay
        streakCalculator.compute(startDay, relapseDays, todayEpochDay)
    }

    val uiState: StateFlow<JourneyUiState> = combine(
        habitRepository.observeHabits(),
        habitRepository.observeCompletionsSince(todayEpochDay - 13),
        checkInRepository.observeSince(todayEpochDay - 13),
        streakFlow,
        profileRepository.plan,
    ) { habits, completions, checkIns, streak, plan ->
        val thisWeekFrom = todayEpochDay - 6
        val lastWeekFrom = todayEpochDay - 13
        val lastWeekTo = todayEpochDay - 7

        val createdDays = habits.associate { habit ->
            habit.id to Instant.ofEpochMilli(habit.createdAtEpochMillis)
                .atZone(zone).toLocalDate().toEpochDay()
        }

        val thisWeek = statsCalculator.compute(
            habits = habits,
            completions = completions.filter { it.epochDay >= thisWeekFrom },
            fromEpochDay = thisWeekFrom,
            toEpochDay = todayEpochDay,
            habitCreatedDays = createdDays,
        )
        val lastWeek = statsCalculator.compute(
            habits = habits,
            completions = completions.filter { it.epochDay in lastWeekFrom..lastWeekTo },
            fromEpochDay = lastWeekFrom,
            toEpochDay = lastWeekTo,
            habitCreatedDays = createdDays,
        )

        val doneSet = completions.map { it.habitId to it.epochDay }.toSet()
        val scheduledToday = habits.filter { it.isScheduledOn(todayEpochDay) }
        val todaysHabits = scheduledToday.map { habit ->
            val createdDay = createdDays[habit.id] ?: todayEpochDay
            HabitWithStatus(
                habit = habit,
                doneToday = habit.id to todayEpochDay in doneSet,
                weekDots = (thisWeekFrom..todayEpochDay).map { day ->
                    when {
                        day < createdDay || !habit.isScheduledOn(day) -> HabitDayDot.OFF
                        habit.id to day in doneSet -> HabitDayDot.DONE
                        else -> HabitDayDot.OPEN
                    }
                },
            )
        }

        JourneyUiState.Ready(
            todayEpochDay = todayEpochDay,
            todaysHabits = todaysHabits,
            otherHabits = habits.filter { !it.isScheduledOn(todayEpochDay) },
            weekDays = thisWeek.days,
            weekCompleted = thisWeek.totalCompleted,
            weekScheduled = thisWeek.totalScheduled,
            insights = insightGenerator.generate(
                thisWeek = thisWeek,
                lastWeek = lastWeek,
                checkInsThisWeek = checkIns.filter { it.epochDay >= thisWeekFrom },
                checkInsLastWeek = checkIns.filter { it.epochDay in lastWeekFrom..lastWeekTo },
                streak = streak,
                milestoneDays = plan?.firstMilestoneDays ?: 7,
                todayEpochDay = todayEpochDay,
            ),
            recoveryScore = recoveryScoreCalculator.compute(streak),
            comebackAchievements = streak.unlockedComebackAchievements(),
        ) as JourneyUiState
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = JourneyUiState.Loading,
    )

    fun onToggleToday(habitId: Long, done: Boolean) {
        viewModelScope.launch {
            habitRepository.setCompleted(
                habitId = habitId,
                epochDay = LocalDate.now().toEpochDay(), // stamped at action time
                completed = done,
            )
        }
    }
}

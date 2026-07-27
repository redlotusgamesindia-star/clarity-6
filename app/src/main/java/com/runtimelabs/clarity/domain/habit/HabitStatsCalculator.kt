package com.runtimelabs.clarity.domain.habit

import com.runtimelabs.clarity.domain.model.Habit
import com.runtimelabs.clarity.domain.model.HabitCompletion
import com.runtimelabs.clarity.domain.model.isScheduledOn
import javax.inject.Inject

/** Per-day totals across all habits. */
data class DayStat(
    val epochDay: Long,
    val scheduled: Int,
    val completed: Int,
)

/** Per-habit totals across the window. */
data class HabitRate(
    val habitId: Long,
    val habitName: String,
    val scheduled: Int,
    val completed: Int,
) {
    /** 0..100; only meaningful when [scheduled] > 0. */
    val percent: Int get() = if (scheduled == 0) 0 else (completed * 100) / scheduled
}

data class HabitWindowStats(
    val days: List<DayStat>,
    val perHabit: List<HabitRate>,
) {
    val totalScheduled: Int get() = days.sumOf { it.scheduled }
    val totalCompleted: Int get() = days.sumOf { it.completed }
}

/**
 * One aggregation, two consumers (the week chart and the insight rules), so
 * they can never disagree about what "completed 4 of 6" means. Pure and
 * deterministic; habits created mid-window only count from their creation
 * day — a habit made on Thursday can't "miss" Monday.
 */
class HabitStatsCalculator @Inject constructor() {

    fun compute(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        fromEpochDay: Long,
        toEpochDay: Long,
        /** Habit id -> epochDay it was created (gates back-missing). */
        habitCreatedDays: Map<Long, Long>,
    ): HabitWindowStats {
        val done: Set<Pair<Long, Long>> = completions.map { it.habitId to it.epochDay }.toSet()

        val days = (fromEpochDay..toEpochDay).map { day ->
            var scheduled = 0
            var completed = 0
            for (habit in habits) {
                val createdDay = habitCreatedDays[habit.id] ?: fromEpochDay
                if (day < createdDay) continue
                if (!habit.isScheduledOn(day)) continue
                scheduled++
                if (habit.id to day in done) completed++
            }
            DayStat(epochDay = day, scheduled = scheduled, completed = completed)
        }

        val perHabit = habits.map { habit ->
            val createdDay = habitCreatedDays[habit.id] ?: fromEpochDay
            var scheduled = 0
            var completed = 0
            for (day in maxOf(fromEpochDay, createdDay)..toEpochDay) {
                if (!habit.isScheduledOn(day)) continue
                scheduled++
                if (habit.id to day in done) completed++
            }
            HabitRate(habit.id, habit.name, scheduled, completed)
        }

        return HabitWindowStats(days = days, perHabit = perHabit)
    }
}

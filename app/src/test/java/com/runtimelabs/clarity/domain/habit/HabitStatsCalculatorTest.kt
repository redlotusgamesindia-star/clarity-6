package com.runtimelabs.clarity.domain.habit

import com.runtimelabs.clarity.domain.model.Habit
import com.runtimelabs.clarity.domain.model.HabitCompletion
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Test

class HabitStatsCalculatorTest {

    private val calculator = HabitStatsCalculator()

    // Epoch day 0 = Thursday 1970-01-01; day 4 = Monday.
    private val monday = 4L

    private fun habit(id: Long, mask: Int = Habit.ALL_DAYS_MASK) = Habit(
        id = id,
        name = "Habit $id",
        iconCode = "spa",
        daysMask = mask,
        reminderMinutesOfDay = null,
        createdAtEpochMillis = 0L,
    )

    @Test
    fun `daily habit over one week counts every day as scheduled`() {
        val h = habit(1)
        val done = listOf(HabitCompletion(1, monday), HabitCompletion(1, monday + 2))
        val stats = calculator.compute(
            habits = listOf(h),
            completions = done,
            fromEpochDay = monday,
            toEpochDay = monday + 6,
            habitCreatedDays = mapOf(1L to 0L),
        )
        assertEquals(7, stats.totalScheduled)
        assertEquals(2, stats.totalCompleted)
        assertEquals(List(7) { 1 }, stats.days.map { it.scheduled })
        assertEquals(listOf(1, 0, 1, 0, 0, 0, 0), stats.days.map { it.completed })
        assertEquals(2, stats.perHabit.single().completed)
        assertEquals(28, stats.perHabit.single().percent) // 2/7
    }

    @Test
    fun `weekday mask limits scheduling to those days`() {
        val weekdays = Habit.maskBit(DayOfWeek.MONDAY) or
            Habit.maskBit(DayOfWeek.TUESDAY) or
            Habit.maskBit(DayOfWeek.WEDNESDAY) or
            Habit.maskBit(DayOfWeek.THURSDAY) or
            Habit.maskBit(DayOfWeek.FRIDAY)
        val stats = calculator.compute(
            habits = listOf(habit(1, mask = weekdays)),
            completions = emptyList(),
            fromEpochDay = monday,
            toEpochDay = monday + 6, // Mon..Sun
            habitCreatedDays = mapOf(1L to 0L),
        )
        assertEquals(5, stats.totalScheduled)
        assertEquals(0, stats.days.last().scheduled)      // Sunday
        assertEquals(0, stats.days[stats.days.size - 2].scheduled) // Saturday
    }

    @Test
    fun `habit created mid-window cannot miss earlier days`() {
        val stats = calculator.compute(
            habits = listOf(habit(1)),
            completions = emptyList(),
            fromEpochDay = monday,
            toEpochDay = monday + 6,
            habitCreatedDays = mapOf(1L to monday + 4), // created Friday
        )
        assertEquals(3, stats.totalScheduled) // Fri, Sat, Sun only
        assertEquals(3, stats.perHabit.single().scheduled)
    }

    @Test
    fun `multiple habits aggregate per day`() {
        val stats = calculator.compute(
            habits = listOf(habit(1), habit(2)),
            completions = listOf(HabitCompletion(1, monday), HabitCompletion(2, monday)),
            fromEpochDay = monday,
            toEpochDay = monday + 1,
            habitCreatedDays = mapOf(1L to 0L, 2L to 0L),
        )
        assertEquals(DayStat(monday, scheduled = 2, completed = 2), stats.days.first())
        assertEquals(DayStat(monday + 1, scheduled = 2, completed = 0), stats.days.last())
    }

    @Test
    fun `no habits yields empty window`() {
        val stats = calculator.compute(emptyList(), emptyList(), monday, monday + 6, emptyMap())
        assertEquals(0, stats.totalScheduled)
        assertEquals(7, stats.days.size)
    }
}

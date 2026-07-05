package com.runtimelabs.clarity.data.notifications

import com.runtimelabs.clarity.domain.model.Habit
import java.time.DayOfWeek
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderTimeCalculatorTest {

    private val calculator = ReminderTimeCalculator()

    // 2024-01-01 is a Monday.
    private fun monday(hour: Int, minute: Int = 0): ZonedDateTime =
        ZonedDateTime.of(2024, 1, 1, hour, minute, 0, 0, ZoneOffset.UTC)

    @Test
    fun `later today when time has not passed`() {
        val trigger = calculator.nextTriggerAtMillis(
            daysMask = Habit.ALL_DAYS_MASK,
            minuteOfDay = 20 * 60, // 20:00
            after = monday(10),
        )
        assertEquals(monday(20).toInstant().toEpochMilli(), trigger)
    }

    @Test
    fun `tomorrow when time already passed today`() {
        val trigger = calculator.nextTriggerAtMillis(Habit.ALL_DAYS_MASK, 9 * 60, monday(10))
        assertEquals(monday(9).plusDays(1).toInstant().toEpochMilli(), trigger)
    }

    @Test
    fun `exact boundary counts as passed`() {
        val trigger = calculator.nextTriggerAtMillis(Habit.ALL_DAYS_MASK, 10 * 60, monday(10))
        assertEquals(monday(10).plusDays(1).toInstant().toEpochMilli(), trigger)
    }

    @Test
    fun `skips to the next scheduled weekday`() {
        val wednesdayOnly = Habit.maskBit(DayOfWeek.WEDNESDAY)
        val trigger = calculator.nextTriggerAtMillis(wednesdayOnly, 8 * 60, monday(10))
        assertEquals(monday(8).plusDays(2).toInstant().toEpochMilli(), trigger)
    }

    @Test
    fun `same weekday next week when today's slot already passed`() {
        val mondayOnly = Habit.maskBit(DayOfWeek.MONDAY)
        val trigger = calculator.nextTriggerAtMillis(mondayOnly, 8 * 60, monday(10))
        assertEquals(monday(8).plusDays(7).toInstant().toEpochMilli(), trigger)
    }
}

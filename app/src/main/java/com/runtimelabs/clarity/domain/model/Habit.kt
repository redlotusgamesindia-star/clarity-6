package com.runtimelabs.clarity.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * A positive replacement routine. Scheduling is a 7-bit weekday mask
 * (bit 0 = Monday .. bit 6 = Sunday, ISO order) — compact, queryable, and
 * timezone-decisions stay at the edges where dates are stamped.
 */
data class Habit(
    val id: Long,
    val name: String,
    /** Stable code from the curated icon set; UI maps to a vector. */
    val iconCode: String,
    /** Bit (isoDayOfWeek - 1) set = scheduled that weekday. */
    val daysMask: Int,
    /** Minutes since local midnight, or null when the reminder is off. */
    val reminderMinutesOfDay: Int?,
    val createdAtEpochMillis: Long,
) {
    init {
        require(daysMask in 1..ALL_DAYS_MASK) { "at least one scheduled day required" }
        require(reminderMinutesOfDay == null || reminderMinutesOfDay in 0..MAX_MINUTE_OF_DAY)
    }

    companion object {
        const val NEW_ID = -1L
        const val ALL_DAYS_MASK = 0b1111111
        const val MAX_MINUTE_OF_DAY = 24 * 60 - 1

        fun maskBit(day: DayOfWeek): Int = 1 shl (day.value - 1)
    }
}

fun Habit.isScheduledOn(day: DayOfWeek): Boolean = daysMask and Habit.maskBit(day) != 0

fun Habit.isScheduledOn(epochDay: Long): Boolean =
    isScheduledOn(LocalDate.ofEpochDay(epochDay).dayOfWeek)

/** One row per habit per completed day; absence means not done. */
data class HabitCompletion(
    val habitId: Long,
    val epochDay: Long,
)

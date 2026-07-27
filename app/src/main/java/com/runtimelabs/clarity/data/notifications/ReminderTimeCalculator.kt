package com.runtimelabs.clarity.data.notifications

import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * Next trigger strictly after [after] for a weekday-mask + minute-of-day
 * reminder. Pure given its inputs (the caller supplies "now"), so the
 * scheduling math is unit-testable without alarms or clocks. DST gaps are
 * resolved by java.time's zone rules when the local time is materialized.
 */
class ReminderTimeCalculator @Inject constructor() {

    fun nextTriggerAtMillis(daysMask: Int, minuteOfDay: Int, after: ZonedDateTime): Long {
        val time = LocalTime.of(minuteOfDay / 60, minuteOfDay % 60)
        var candidate = after.toLocalDate().atTime(time).atZone(after.zone)
        if (!candidate.isAfter(after)) {
            candidate = candidate.plusDays(1)
        }
        repeat(7) {
            val bit = 1 shl (candidate.dayOfWeek.value - 1)
            if (daysMask and bit != 0) return candidate.toInstant().toEpochMilli()
            candidate = candidate.plusDays(1)
        }
        // Unreachable with a valid mask (Habit requires >= 1 day); safe fallback.
        return candidate.toInstant().toEpochMilli()
    }
}

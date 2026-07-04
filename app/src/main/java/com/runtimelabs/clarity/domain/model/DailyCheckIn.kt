package com.runtimelabs.clarity.domain.model

/**
 * Five levels, stored as stable Ints (1..5). Five is enough resolution for a
 * meaningful weekly pattern without turning a 10-second ritual into a survey.
 */
enum class MoodLevel(val storageValue: Int) {
    STRUGGLING(1),
    LOW(2),
    OKAY(3),
    GOOD(4),
    GREAT(5);

    companion object {
        fun fromStorageValue(value: Int): MoodLevel =
            entries.firstOrNull { it.storageValue == value } ?: OKAY
    }
}

/**
 * One row per local calendar day ([epochDay] is the primary key upstream).
 * Re-checking-in the same day updates the row — feelings change, and editing
 * beats guilt about having "answered wrong".
 *
 * Deliberately NOT part of the streak: a streak measures days clean, which
 * accrues by living them, not by logging them. Missing a check-in must never
 * punish (no-shame rule).
 */
data class DailyCheckIn(
    val epochDay: Long,
    val mood: MoodLevel,
    /** 0 = no urges today .. 10 = intense. Self-reported. */
    val urgeLevel: Int,
    val updatedAtEpochMillis: Long,
) {
    init {
        require(urgeLevel in 0..10) { "urgeLevel must be in 0..10" }
    }
}

package com.runtimelabs.clarity.domain.recovery

enum class RecoveryMotivationCode {
    DAY_1, DAY_2, DAY_3, DAY_4, DAY_5, DAY_6, DAY_7,
    WEEK_2, WEEK_3, MONTH_1,
    ONGOING,
}

/**
 * Maps a day number in a post-relapse run to a message code. Pure lookup,
 * no state, no injection needed — a plain object rather than a class, same
 * as [com.runtimelabs.clarity.data.notifications.ReminderTimeCalculator]'s
 * sibling utilities that don't hold dependencies.
 */
object RecoveryMotivationMessages {
    fun forDay(day: Int): RecoveryMotivationCode = when (day) {
        1 -> RecoveryMotivationCode.DAY_1
        2 -> RecoveryMotivationCode.DAY_2
        3 -> RecoveryMotivationCode.DAY_3
        4 -> RecoveryMotivationCode.DAY_4
        5 -> RecoveryMotivationCode.DAY_5
        6 -> RecoveryMotivationCode.DAY_6
        7 -> RecoveryMotivationCode.DAY_7
        14 -> RecoveryMotivationCode.WEEK_2
        21 -> RecoveryMotivationCode.WEEK_3
        30 -> RecoveryMotivationCode.MONTH_1
        else -> RecoveryMotivationCode.ONGOING
    }
}

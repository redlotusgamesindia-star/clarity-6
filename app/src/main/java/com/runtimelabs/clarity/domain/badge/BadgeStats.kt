package com.runtimelabs.clarity.domain.badge

/**
 * Everything [BadgeEvaluator] needs, pre-derived by the caller. Same
 * contract as [com.runtimelabs.clarity.domain.streak.StreakCalculator] and
 * every other decision engine in this app: plain data in, no clock, no I/O,
 * no Android types — timezone-aware conversions (epoch millis -> local
 * hour, profile creation time -> epoch day) happen in the data layer
 * *before* this reaches the evaluator, not inside it.
 */
data class BadgeStats(
    /** Best streak ever reached (StreakSnapshot.longestDays) — see [Badge]'s doc comment for why. */
    val longestStreakDays: Int,
    /** Count of relapse events ever recorded — each one is the start of a new recovery run. */
    val totalRelapses: Int,
    /** True if any daily check-in was ever saved before local noon. */
    val hasMorningCheckIn: Boolean,
    /** Total journal entries ever written (free-writing entries only — see [Badge.JOURNAL_WRITER]). */
    val journalEntryCount: Int,
    /** Longest run of consecutive calendar days with at least one Emergency Toolkit session. */
    val longestToolkitUsageStreakDays: Int,
)

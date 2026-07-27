package com.runtimelabs.clarity.domain.badge

import javax.inject.Inject

/**
 * Pure and deterministic (AD-4), same contract as every other calculator in
 * this app ([com.runtimelabs.clarity.domain.streak.StreakCalculator],
 * [com.runtimelabs.clarity.domain.toolkit.ToolkitUsageStatsCalculator]):
 * given the current facts, no clock, no I/O, exhaustively unit-testable on
 * the JVM. It answers exactly one question — "given these stats and what's
 * already unlocked, what's newly earned?" — and knows nothing about how the
 * result gets persisted or celebrated; that's [com.runtimelabs.clarity.domain.repository.BadgeRepository]'s job.
 *
 * Threshold choices, recorded here rather than left to guesswork at the
 * call site:
 *  - [JOURNAL_WRITER_ENTRIES] = 5: not a spec'd number, chosen to mirror
 *    [Badge.FIVE_RECOVERIES]'s "five" as a believable "you've formed the
 *    habit" milestone rather than either a trivial one-entry gate or a
 *    high bar that takes weeks to reach.
 *  - [LEARNING_STREAK_DAYS] = 3: mirrors [Badge.DAY_3], the app's own first
 *    non-trivial day-count milestone — reused here as the bar for "a
 *    streak" so the word means the same thing everywhere it appears.
 */
class BadgeEvaluator @Inject constructor() {

    fun evaluate(stats: BadgeStats, alreadyUnlocked: Set<Badge>): List<Badge> {
        val qualifying = mutableListOf<Badge>()

        for (badge in Badge.STREAK_LADDER) {
            if (stats.longestStreakDays >= streakThreshold(badge)) qualifying += badge
        }
        if (stats.totalRelapses >= 1) qualifying += Badge.FIRST_RECOVERY
        if (stats.totalRelapses >= 5) qualifying += Badge.FIVE_RECOVERIES
        if (stats.hasMorningCheckIn) qualifying += Badge.MORNING_CHECK_IN
        if (stats.journalEntryCount >= JOURNAL_WRITER_ENTRIES) qualifying += Badge.JOURNAL_WRITER
        if (stats.longestToolkitUsageStreakDays >= LEARNING_STREAK_DAYS) qualifying += Badge.LEARNING_STREAK

        return qualifying.filterNot { it in alreadyUnlocked }
    }

    private fun streakThreshold(badge: Badge): Int = when (badge) {
        Badge.DAY_1 -> 1
        Badge.DAY_3 -> 3
        Badge.DAY_7 -> 7
        Badge.DAY_14 -> 14
        Badge.DAY_21 -> 21
        Badge.DAY_30 -> 30
        Badge.DAY_50 -> 50
        Badge.DAY_100 -> 100
        Badge.DAY_365 -> 365
        else -> error("$badge is not a streak badge")
    }

    companion object {
        const val JOURNAL_WRITER_ENTRIES = 5
        const val LEARNING_STREAK_DAYS = 3
    }
}

/**
 * The length of the longest run of consecutive calendar days present in
 * [epochDays] (duplicates on the same day collapse to one). Extracted as a
 * standalone top-level function — same reasoning as
 * [com.runtimelabs.clarity.domain.recovery.unlockedComebackAchievements]
 * being a free function rather than a method: it's pure list math with no
 * need for a class or injection, and it's independently unit-testable
 * without constructing a [BadgeStats].
 */
fun longestConsecutiveRun(epochDays: List<Long>): Int {
    if (epochDays.isEmpty()) return 0
    val sorted = epochDays.distinct().sorted()
    var longest = 1
    var current = 1
    for (i in 1 until sorted.size) {
        current = if (sorted[i] == sorted[i - 1] + 1) current + 1 else 1
        if (current > longest) longest = current
    }
    return longest
}

package com.runtimelabs.clarity.domain.streak

import com.runtimelabs.clarity.domain.model.StreakSnapshot
import javax.inject.Inject

/**
 * Derives streaks from the journey's facts. Same contract as the plan
 * generator (AD-4): pure, deterministic, exhaustively unit-tested, no clock
 * access — "today" is a parameter, so tests never flake and midnight bugs
 * can't hide.
 *
 * SEMANTICS (documented in ARCHITECTURE.md §17):
 *  - A clean run starts the day AFTER a relapse. Relapsing today reads 0;
 *    tomorrow reads "Day 1". No run exists until the relapse day has passed.
 *  - The very first run starts ON the recovery start day (onboarding
 *    completion counts as Day 1 that evening — commitment deserves credit).
 *  - Today is always counted while clean (inclusive ongoing run).
 *  - A closed run between clean-start s and relapse day r has length r - s
 *    (days s .. r-1 were clean; the relapse day is not).
 */
class StreakCalculator @Inject constructor() {

    fun compute(
        recoveryStartEpochDay: Long,
        relapseEpochDays: List<Long>,
        todayEpochDay: Long,
    ): StreakSnapshot {
        // Defensive normalization: sorted, unique, within [start, today].
        // Future-dated or pre-start rows are data errors; clamp, don't crash.
        val relapses = relapseEpochDays
            .filter { it in recoveryStartEpochDay..todayEpochDay }
            .distinct()
            .sorted()

        var runStart = recoveryStartEpochDay
        var longest = 0
        for (relapseDay in relapses) {
            val closedRun = (relapseDay - runStart).toInt()
            if (closedRun > longest) longest = closedRun
            runStart = relapseDay + 1
        }

        val current = (todayEpochDay - runStart + 1).toInt().coerceAtLeast(0)
        if (current > longest) longest = current

        return StreakSnapshot(
            currentDays = current,
            longestDays = longest,
            cleanSinceEpochDay = runStart,
        )
    }
}

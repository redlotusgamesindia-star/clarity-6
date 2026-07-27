package com.runtimelabs.clarity.domain.toolkit

import javax.inject.Inject

/**
 * Pure and deterministic (AD-4), same contract as every other calculator in
 * this app: given the full record list, no clock, no I/O. [averageDurationSeconds]
 * is computed only from records with a real observed duration — see
 * [ToolkitUsageRecord]'s doc comment for why zero-duration entries (the
 * journal shortcut) are excluded rather than averaged in as zero.
 */
class ToolkitUsageStatsCalculator @Inject constructor() {

    fun compute(records: List<ToolkitUsageRecord>): ToolkitUsageStats {
        if (records.isEmpty()) {
            return ToolkitUsageStats(timesUsed = 0, mostUsedTool = null, averageDurationSeconds = 0)
        }

        val mostUsed = records
            .groupingBy { it.tool }
            .eachCount()
            .entries
            .minWithOrNull(compareBy<Map.Entry<ToolkitTool, Int>> { -it.value }.thenBy { it.key.ordinal })
            ?.key

        val timedRecords = records.filter { it.durationSeconds > 0 }
        val averageDuration = if (timedRecords.isEmpty()) {
            0
        } else {
            timedRecords.sumOf { it.durationSeconds } / timedRecords.size
        }

        return ToolkitUsageStats(
            timesUsed = records.size,
            mostUsedTool = mostUsed,
            averageDurationSeconds = averageDuration,
        )
    }
}

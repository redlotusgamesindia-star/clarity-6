package com.runtimelabs.clarity.domain.toolkit

/**
 * One completed (or meaningfully-attempted) use of a toolkit tool.
 * [durationSeconds] is honest, not padded: tools this app can actually
 * observe start-to-finish (breathing, the walk timer, reminder tools,
 * guided steps, the motivation wall) report a real elapsed time. The
 * journal shortcut hands off to a general-purpose editor this screen has
 * no visibility into once you leave — it reports 0 rather than a made-up
 * number, and [ToolkitUsageStatsCalculator] excludes zero-duration
 * records from the average rather than let them silently drag it down.
 */
data class ToolkitUsageRecord(
    val id: Long,
    val tool: ToolkitTool,
    val startedAtEpochMillis: Long,
    val durationSeconds: Int,
    val epochDay: Long,
)

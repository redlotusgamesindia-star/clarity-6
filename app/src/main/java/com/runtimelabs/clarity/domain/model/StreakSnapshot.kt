package com.runtimelabs.clarity.domain.model

/**
 * Everything the UI needs to render streak state. Pure derived data — there
 * is deliberately no stored counter anywhere that could drift from the truth.
 */
data class StreakSnapshot(
    /** Days clean in the current run, counting today. 0 only on a relapse day. */
    val currentDays: Int,
    /** Best run ever, including the current one if it leads. */
    val longestDays: Int,
    /** First day of the current clean run (epoch day). */
    val cleanSinceEpochDay: Long,
)

package com.runtimelabs.clarity.domain.model

/**
 * Everything the UI needs to render streak state. Pure derived data — there
 * is deliberately no stored counter anywhere that could drift from the truth.
 *
 * Extended in Phase E for the recovery flow: [previousRunDays] and
 * [bestClosedRunDays] distinguish "the run that just ended" from "the best
 * run ever", which the Rebuild System and comeback achievements both need
 * as two different numbers. [totalRelapses] and [totalCleanDays] feed the
 * Recovery Score. None of this is new state — it all falls out of the same
 * relapse-event list [StreakCalculator] already walked for [longestDays].
 */
data class StreakSnapshot(
    /** Days clean in the current run, counting today. 0 only on a relapse day. */
    val currentDays: Int,
    /** Best run ever, including the current one if it leads. */
    val longestDays: Int,
    /** First day of the current clean run (epoch day). */
    val cleanSinceEpochDay: Long,
    /** Length of the most recently CLOSED run; null if there has never been one. */
    val previousRunDays: Int?,
    /** Best CLOSED run length only (excludes the current ongoing run); 0 if none yet. */
    val bestClosedRunDays: Int,
    /** Count of relapse events ever recorded. */
    val totalRelapses: Int,
    /** Every clean day ever lived, across every closed run plus the current one. */
    val totalCleanDays: Int,
) {
    /** True once the current run has genuinely gone longer than the old record. */
    val hasBeatenPreviousRecord: Boolean get() = totalRelapses > 0 && currentDays > bestClosedRunDays

    /** True while rebuilding toward (but not yet past) the pre-relapse best. */
    val isRebuilding: Boolean get() = totalRelapses > 0 && !hasBeatenPreviousRecord
}

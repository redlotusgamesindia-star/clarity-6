package com.runtimelabs.clarity.domain.model

/**
 * Everything the user told us during onboarding — the single input to the
 * plan generator and, later, to the recovery-score and insight engines.
 * Immutable snapshot; retaking the assessment (future settings option)
 * replaces it wholesale.
 */
data class RecoveryProfile(
    val ageRange: AgeRange,
    val gender: GenderIdentity,
    val yearsAddicted: YearsAddicted,
    val frequency: UsageFrequency,
    val mainTrigger: MainTrigger,
    val goal: RecoveryGoal,
    val motivationLevel: Int,          // 1..10, user-reported
    val reasonsToQuit: List<ReasonToQuit>,
    val previousStreak: PreviousStreak,
    val strongestUrgeTime: UrgeTime,
    val sleepSchedule: SleepSchedule,
    val createdAtEpochMillis: Long,
) {
    init {
        require(motivationLevel in 1..10) { "motivationLevel must be in 1..10" }
        require(reasonsToQuit.isNotEmpty()) { "at least one reason is required" }
    }
}

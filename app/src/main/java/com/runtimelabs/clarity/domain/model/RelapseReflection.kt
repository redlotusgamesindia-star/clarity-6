package com.runtimelabs.clarity.domain.model

/**
 * Optional context captured after a relapse: what triggered it, when, how it
 * felt, where. Every field is nullable/blank-tolerant on purpose — this is
 * offered support, not an interrogation someone must complete to move on.
 * Linked to its [JourneyEvent] by id so the append-only timeline stays the
 * single source of truth for "a relapse happened here"; this table only
 * ever adds color to a fact that's already recorded.
 */
data class RelapseReflection(
    val id: Long,
    val journeyEventId: Long,
    val epochDay: Long,
    val createdAtEpochMillis: Long,
    val trigger: MainTrigger?,
    val timeOfDay: UrgeTime?,
    val mood: MoodLevel?,
    val location: RelapseLocation?,
    val notes: String,
) {
    companion object {
        const val NEW_ID = -1L
    }
}

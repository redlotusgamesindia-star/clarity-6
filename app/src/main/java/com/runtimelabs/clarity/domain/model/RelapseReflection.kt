package com.runtimelabs.clarity.domain.model

/**
 * What happened, how it felt, and what triggered it — the three questions
 * the recovery flow asks, in that order. Every field is nullable on
 * purpose: this is offered support, not an interrogation someone must
 * complete to move on. Linked to its [JourneyEvent] by id so the
 * append-only timeline stays the single source of truth for "a relapse
 * happened here"; this table only ever adds color to a fact that's
 * already recorded.
 */
data class RelapseReflection(
    val id: Long,
    val journeyEventId: Long,
    val epochDay: Long,
    val createdAtEpochMillis: Long,
    val setbackType: RelapseSetbackType?,
    val emotion: RelapseEmotion?,
    val trigger: RelapseTrigger?,
) {
    companion object {
        const val NEW_ID = -1L
    }
}

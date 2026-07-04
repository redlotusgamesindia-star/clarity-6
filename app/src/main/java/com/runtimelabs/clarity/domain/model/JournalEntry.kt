package com.runtimelabs.clarity.domain.model

/**
 * Free writing. No title field on purpose — a title prompt is friction, and
 * friction is the enemy of the habit. The list shows date + first lines.
 */
data class JournalEntry(
    val id: Long,
    val epochDay: Long,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val body: String,
) {
    companion object {
        /** Sentinel for a not-yet-persisted entry. */
        const val NEW_ID = -1L
    }
}

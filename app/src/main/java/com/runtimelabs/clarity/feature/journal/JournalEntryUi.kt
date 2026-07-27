package com.runtimelabs.clarity.feature.journal

import com.runtimelabs.clarity.domain.model.GratitudeEntry
import com.runtimelabs.clarity.domain.model.JournalEntry
import com.runtimelabs.clarity.domain.model.ThoughtRecord

/** Which sheet/screen a hub row opens to. */
enum class JournalEntryKind { FREE, THOUGHT, GRATITUDE }

/**
 * One shape for the mixed-kind list. Free entries preview their body;
 * thought records preview the situation; gratitude previews the first item.
 * Each wraps its full domain object so opening a row needs no re-fetch.
 */
data class JournalHubEntry(
    val id: Long,
    val kind: JournalEntryKind,
    val createdAtEpochMillis: Long,
    val epochDay: Long,
    val preview: String,
) {
    companion object {
        fun from(entry: JournalEntry) = JournalHubEntry(
            id = entry.id,
            kind = JournalEntryKind.FREE,
            createdAtEpochMillis = entry.createdAtEpochMillis,
            epochDay = entry.epochDay,
            preview = entry.body,
        )

        fun from(record: ThoughtRecord) = JournalHubEntry(
            id = record.id,
            kind = JournalEntryKind.THOUGHT,
            createdAtEpochMillis = record.createdAtEpochMillis,
            epochDay = record.epochDay,
            preview = record.situation,
        )

        fun from(entry: GratitudeEntry) = JournalHubEntry(
            id = entry.id,
            kind = JournalEntryKind.GRATITUDE,
            createdAtEpochMillis = entry.createdAtEpochMillis,
            epochDay = entry.epochDay,
            preview = entry.first,
        )
    }
}

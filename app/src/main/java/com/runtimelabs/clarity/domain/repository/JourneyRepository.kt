package com.runtimelabs.clarity.domain.repository

import com.runtimelabs.clarity.domain.model.JourneyEvent
import com.runtimelabs.clarity.domain.model.JourneyEventType
import kotlinx.coroutines.flow.Flow

interface JourneyRepository {
    /** Epoch days of all events of [type], ascending. Streak input. */
    fun observeEventDays(type: JourneyEventType): Flow<List<Long>>

    /**
     * Append a fact to the timeline. Events are never edited or deleted.
     * Returns the new row's id, needed when something else (a relapse's
     * reflection) needs to link back to this specific event.
     */
    suspend fun record(event: JourneyEvent): Long
}

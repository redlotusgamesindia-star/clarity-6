package com.runtimelabs.clarity.domain.repository

import com.runtimelabs.clarity.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    /** All entries, newest first. */
    fun observeAll(): Flow<List<JournalEntry>>

    suspend fun getById(id: Long): JournalEntry?

    /** Returns the persisted id (new for NEW_ID inserts, unchanged otherwise). */
    suspend fun save(entry: JournalEntry): Long

    suspend fun delete(id: Long)
}

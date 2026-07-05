package com.runtimelabs.clarity.domain.repository

import com.runtimelabs.clarity.domain.model.ThoughtRecord
import kotlinx.coroutines.flow.Flow

interface ThoughtRecordRepository {
    fun observeAll(): Flow<List<ThoughtRecord>>
    suspend fun getById(id: Long): ThoughtRecord?
    suspend fun save(record: ThoughtRecord): Long
    suspend fun delete(id: Long)
}

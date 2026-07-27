package com.runtimelabs.clarity.domain.repository

import com.runtimelabs.clarity.domain.model.GratitudeEntry
import kotlinx.coroutines.flow.Flow

interface GratitudeRepository {
    fun observeAll(): Flow<List<GratitudeEntry>>
    suspend fun getById(id: Long): GratitudeEntry?
    suspend fun save(entry: GratitudeEntry): Long
    suspend fun delete(id: Long)
}

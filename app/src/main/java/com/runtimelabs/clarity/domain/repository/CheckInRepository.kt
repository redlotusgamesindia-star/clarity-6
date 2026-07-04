package com.runtimelabs.clarity.domain.repository

import com.runtimelabs.clarity.domain.model.DailyCheckIn
import kotlinx.coroutines.flow.Flow

interface CheckInRepository {
    /** Check-ins with epochDay >= [sinceEpochDay], oldest first. */
    fun observeSince(sinceEpochDay: Long): Flow<List<DailyCheckIn>>

    /** Insert or replace the row for the check-in's day. */
    suspend fun upsert(checkIn: DailyCheckIn)
}

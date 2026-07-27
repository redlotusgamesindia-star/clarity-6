package com.runtimelabs.clarity.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.runtimelabs.clarity.data.local.db.entity.DailyCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {

    @Query("SELECT * FROM daily_checkin WHERE epochDay >= :sinceEpochDay ORDER BY epochDay ASC")
    fun observeSince(sinceEpochDay: Long): Flow<List<DailyCheckInEntity>>

    @Upsert
    suspend fun upsert(checkIn: DailyCheckInEntity)
}

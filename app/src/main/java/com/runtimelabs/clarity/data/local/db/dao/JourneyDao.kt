package com.runtimelabs.clarity.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.runtimelabs.clarity.data.local.db.entity.JourneyEventEntity
import kotlinx.coroutines.flow.Flow

/** Append-only on purpose — the timeline records facts, it doesn't revise them. */
@Dao
interface JourneyDao {

    @Query("SELECT epochDay FROM journey_event WHERE type = :type ORDER BY epochDay ASC")
    fun observeEventDays(type: String): Flow<List<Long>>

    @Insert
    suspend fun insert(event: JourneyEventEntity): Long
}

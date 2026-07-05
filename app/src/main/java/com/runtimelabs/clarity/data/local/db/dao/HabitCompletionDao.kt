package com.runtimelabs.clarity.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.runtimelabs.clarity.data.local.db.entity.HabitCompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {

    @Query("SELECT * FROM habit_completion WHERE epochDay >= :sinceEpochDay")
    fun observeSince(sinceEpochDay: Long): Flow<List<HabitCompletionEntity>>

    @Upsert
    suspend fun upsert(completion: HabitCompletionEntity)

    @Query("DELETE FROM habit_completion WHERE habitId = :habitId AND epochDay = :epochDay")
    suspend fun delete(habitId: Long, epochDay: Long)
}

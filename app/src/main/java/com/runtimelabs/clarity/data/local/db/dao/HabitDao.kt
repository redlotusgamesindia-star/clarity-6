package com.runtimelabs.clarity.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.runtimelabs.clarity.data.local.db.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habit ORDER BY createdAtEpochMillis ASC")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit")
    suspend fun getAllOnce(): List<HabitEntity>

    @Query("SELECT * FROM habit WHERE id = :id")
    suspend fun getById(id: Long): HabitEntity?

    @Insert
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Query("DELETE FROM habit WHERE id = :id")
    suspend fun deleteHabitRow(id: Long)

    @Query("DELETE FROM habit_completion WHERE habitId = :habitId")
    suspend fun deleteCompletionsFor(habitId: Long)

    /** Habit + its history go together; a crash between must not orphan rows. */
    @Transaction
    suspend fun deleteWithCompletions(id: Long) {
        deleteCompletionsFor(id)
        deleteHabitRow(id)
    }
}

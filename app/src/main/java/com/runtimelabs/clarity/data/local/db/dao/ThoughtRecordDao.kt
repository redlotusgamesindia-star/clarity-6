package com.runtimelabs.clarity.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.runtimelabs.clarity.data.local.db.entity.ThoughtRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThoughtRecordDao {

    @Query("SELECT * FROM thought_record ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<ThoughtRecordEntity>>

    @Query("SELECT * FROM thought_record WHERE id = :id")
    suspend fun getById(id: Long): ThoughtRecordEntity?

    @Insert
    suspend fun insert(record: ThoughtRecordEntity): Long

    @Update
    suspend fun update(record: ThoughtRecordEntity)

    @Query("DELETE FROM thought_record WHERE id = :id")
    suspend fun deleteById(id: Long)
}

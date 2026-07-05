package com.runtimelabs.clarity.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.runtimelabs.clarity.data.local.db.entity.GratitudeEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GratitudeDao {

    @Query("SELECT * FROM gratitude_entry ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<GratitudeEntryEntity>>

    @Query("SELECT * FROM gratitude_entry WHERE id = :id")
    suspend fun getById(id: Long): GratitudeEntryEntity?

    @Insert
    suspend fun insert(entry: GratitudeEntryEntity): Long

    @Update
    suspend fun update(entry: GratitudeEntryEntity)

    @Query("DELETE FROM gratitude_entry WHERE id = :id")
    suspend fun deleteById(id: Long)
}

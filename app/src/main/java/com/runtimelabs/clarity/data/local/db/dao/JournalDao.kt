package com.runtimelabs.clarity.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.runtimelabs.clarity.data.local.db.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Query("SELECT * FROM journal_entry ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entry WHERE id = :id")
    suspend fun getById(id: Long): JournalEntryEntity?

    @Insert
    suspend fun insert(entry: JournalEntryEntity): Long

    @Update
    suspend fun update(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entry WHERE id = :id")
    suspend fun deleteById(id: Long)
}

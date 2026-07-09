package com.runtimelabs.clarity.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.runtimelabs.clarity.data.local.db.entity.ToolkitUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolkitUsageDao {
    @Insert
    suspend fun insert(usage: ToolkitUsageEntity)

    @Query("SELECT * FROM toolkit_usage ORDER BY startedAtEpochMillis ASC")
    fun observeAll(): Flow<List<ToolkitUsageEntity>>
}

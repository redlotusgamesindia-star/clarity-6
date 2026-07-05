package com.runtimelabs.clarity.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.runtimelabs.clarity.data.local.db.entity.RelapseReflectionEntity

@Dao
interface RelapseReflectionDao {
    @Insert
    suspend fun insert(reflection: RelapseReflectionEntity)
}

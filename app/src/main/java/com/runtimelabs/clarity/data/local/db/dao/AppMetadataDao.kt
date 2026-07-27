package com.runtimelabs.clarity.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.runtimelabs.clarity.data.local.db.entity.AppMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppMetadataDao {

    @Query("SELECT * FROM app_metadata WHERE id = ${AppMetadataEntity.SINGLETON_ID}")
    fun observeMetadata(): Flow<AppMetadataEntity?>

    @Query("SELECT * FROM app_metadata WHERE id = ${AppMetadataEntity.SINGLETON_ID}")
    suspend fun getMetadata(): AppMetadataEntity?

    @Upsert
    suspend fun upsert(metadata: AppMetadataEntity)
}

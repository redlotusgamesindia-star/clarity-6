package com.runtimelabs.clarity.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gratitude_entry")
data class GratitudeEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val first: String,
    val second: String?,
    val third: String?,
)

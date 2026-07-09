package com.runtimelabs.clarity.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "toolkit_usage")
data class ToolkitUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tool: String,
    val startedAtEpochMillis: Long,
    val durationSeconds: Int,
    val epochDay: Long,
)

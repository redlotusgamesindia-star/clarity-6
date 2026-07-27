package com.runtimelabs.clarity.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconCode: String,
    val daysMask: Int,
    val reminderMinutesOfDay: Int?,
    val createdAtEpochMillis: Long,
)

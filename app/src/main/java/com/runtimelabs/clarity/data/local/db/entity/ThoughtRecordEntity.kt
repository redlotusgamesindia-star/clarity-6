package com.runtimelabs.clarity.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "thought_record")
data class ThoughtRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val situation: String,
    val automaticThought: String,
    val feeling: String,
    val feelingIntensity: Int,
    val reframe: String,
)

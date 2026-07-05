package com.runtimelabs.clarity.data.local.db.entity

import androidx.room.Entity

/** Presence of a row = done that day; completion is a fact, not a flag. */
@Entity(tableName = "habit_completion", primaryKeys = ["habitId", "epochDay"])
data class HabitCompletionEntity(
    val habitId: Long,
    val epochDay: Long,
)

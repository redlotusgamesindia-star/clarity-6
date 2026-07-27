package com.runtimelabs.clarity.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One row per local calendar day; upserted, so re-check-ins edit in place. */
@Entity(tableName = "daily_checkin")
data class DailyCheckInEntity(
    @PrimaryKey val epochDay: Long,
    val mood: Int,
    val urgeLevel: Int,
    val updatedAtEpochMillis: Long,
)

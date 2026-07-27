package com.runtimelabs.clarity.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Append-only. No update/delete DAO methods exist for this table, by design. */
@Entity(tableName = "journey_event")
data class JourneyEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val occurredAtEpochMillis: Long,
    val epochDay: Long,
)

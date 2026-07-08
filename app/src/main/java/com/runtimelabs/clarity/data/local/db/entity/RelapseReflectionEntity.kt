package com.runtimelabs.clarity.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * What happened, how it felt, and what triggered it — linked to its
 * [JourneyEventEntity] by id. Every field but the id/link/timestamps is
 * nullable — this table only ever adds color to a fact the journey
 * timeline already owns; it is never the source of truth for "a relapse
 * happened."
 */
@Entity(tableName = "relapse_reflection")
data class RelapseReflectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val journeyEventId: Long,
    val epochDay: Long,
    val createdAtEpochMillis: Long,
    val setbackType: String?,
    val emotion: String?,
    val trigger: String?,
)

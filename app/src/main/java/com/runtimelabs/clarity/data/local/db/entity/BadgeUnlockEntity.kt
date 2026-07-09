package com.runtimelabs.clarity.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per earned badge, keyed by [badge]'s stable storage value — the
 * table IS the "unlocked" flag; a badge with no row here has never been
 * earned. Never updated or deleted once written, same append-only spirit as
 * `journey_event` (§17): a badge earned is a fact about the past, not a
 * toggle.
 */
@Entity(tableName = "badge_unlock")
data class BadgeUnlockEntity(
    @PrimaryKey val badge: String,
    val unlockedAtEpochDay: Long,
    val unlockedAtEpochMillis: Long,
)

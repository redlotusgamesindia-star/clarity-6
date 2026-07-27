package com.runtimelabs.clarity.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table anchoring app-level facts the database itself needs:
 * when the journey began (for "member since"/lifetime stats) and which
 * version of the seeded content (quotes/tips/articles JSON) has been
 * imported — Phase A's content seeder compares this against the bundled
 * asset version and upserts only the delta.
 *
 * This is the only entity in the foundation build. Feature entities
 * (journey_events, check_ins, ...) land with their features in Phase A,
 * each as a Room schema migration from this v1 baseline.
 */
@Entity(tableName = "app_metadata")
data class AppMetadataEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val firstLaunchEpochMillis: Long,
    val seedContentVersion: Int,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}

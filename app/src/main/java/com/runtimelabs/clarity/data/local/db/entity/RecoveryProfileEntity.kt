package com.runtimelabs.clarity.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Singleton row (id = 0): the onboarding profile plus the two pieces of plan
 * metadata (milestone, focus areas) that belong to the plan as a whole rather
 * than any item. Enum answers persist as their stable storageValue strings;
 * multi-value fields are pipe-joined storage values (values are declared
 * without pipes, so no escaping is needed).
 */
@Entity(tableName = "recovery_profile")
data class RecoveryProfileEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val ageRange: String,
    val gender: String?,
    val yearsAddicted: String,
    val frequency: String,
    val mainTrigger: String,
    val goal: String,
    val motivationLevel: Int,
    val reasonsToQuit: String,
    val previousStreak: String,
    val strongestUrgeTime: String,
    val sleepSchedule: String,
    val firstMilestoneDays: Int,
    val focusAreas: String,
    val createdAtEpochMillis: Long,
) {
    companion object {
        const val SINGLETON_ID = 0
        const val LIST_SEPARATOR = "|"
    }
}

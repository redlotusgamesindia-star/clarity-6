package com.runtimelabs.clarity.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per generated plan item. [isCompleted] is unused by onboarding but
 * part of the v2 schema on purpose: the upcoming tasks feature flips it, and
 * shipping the column now avoids a migration whose only change is one flag.
 */
@Entity(tableName = "recovery_plan_item")
data class RecoveryPlanItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val category: String,
    val orderIndex: Int,
    val isCompleted: Boolean = false,
)

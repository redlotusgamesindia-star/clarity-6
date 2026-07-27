package com.runtimelabs.clarity.data.repository

import com.runtimelabs.clarity.data.local.db.entity.RecoveryPlanItemEntity
import com.runtimelabs.clarity.data.local.db.entity.RecoveryProfileEntity
import com.runtimelabs.clarity.data.local.db.entity.RecoveryProfileEntity.Companion.LIST_SEPARATOR
import com.runtimelabs.clarity.domain.model.AgeRange
import com.runtimelabs.clarity.domain.model.GenderIdentity
import com.runtimelabs.clarity.domain.model.MainTrigger
import com.runtimelabs.clarity.domain.model.PlanCategory
import com.runtimelabs.clarity.domain.model.PlanItemCode
import com.runtimelabs.clarity.domain.model.PreviousStreak
import com.runtimelabs.clarity.domain.model.ReasonToQuit
import com.runtimelabs.clarity.domain.model.RecoveryGoal
import com.runtimelabs.clarity.domain.model.RecoveryPlan
import com.runtimelabs.clarity.domain.model.RecoveryPlanItem
import com.runtimelabs.clarity.domain.model.RecoveryProfile
import com.runtimelabs.clarity.domain.model.SleepSchedule
import com.runtimelabs.clarity.domain.model.UrgeTime
import com.runtimelabs.clarity.domain.model.UsageFrequency
import com.runtimelabs.clarity.domain.model.YearsAddicted

/*
 * Entity <-> domain mapping. Reads are defensive: unknown storage values fall
 * back to enum defaults (or are dropped, for list members) instead of
 * crashing — a downgraded app reading a newer row should degrade, not die.
 */

internal fun RecoveryProfile.toEntity(plan: RecoveryPlan): RecoveryProfileEntity =
    RecoveryProfileEntity(
        ageRange = ageRange.storageValue,
        gender = gender.storageValue,
        yearsAddicted = yearsAddicted.storageValue,
        frequency = frequency.storageValue,
        mainTrigger = mainTrigger.storageValue,
        goal = goal.storageValue,
        motivationLevel = motivationLevel,
        reasonsToQuit = reasonsToQuit.joinToString(LIST_SEPARATOR) { it.storageValue },
        previousStreak = previousStreak.storageValue,
        strongestUrgeTime = strongestUrgeTime.storageValue,
        sleepSchedule = sleepSchedule.storageValue,
        firstMilestoneDays = plan.firstMilestoneDays,
        focusAreas = plan.focusAreas.joinToString(LIST_SEPARATOR) { it.storageValue },
        createdAtEpochMillis = createdAtEpochMillis,
    )

internal fun RecoveryProfileEntity.toDomainProfile(): RecoveryProfile =
    RecoveryProfile(
        ageRange = AgeRange.fromStorageValue(ageRange),
        gender = GenderIdentity.fromStorageValue(gender),
        yearsAddicted = YearsAddicted.fromStorageValue(yearsAddicted),
        frequency = UsageFrequency.fromStorageValue(frequency),
        mainTrigger = MainTrigger.fromStorageValue(mainTrigger),
        goal = RecoveryGoal.fromStorageValue(goal),
        motivationLevel = motivationLevel.coerceIn(1, 10),
        reasonsToQuit = reasonsToQuit.split(LIST_SEPARATOR)
            .mapNotNull { ReasonToQuit.fromStorageValue(it) }
            .ifEmpty { listOf(ReasonToQuit.SELF_RESPECT) },
        previousStreak = PreviousStreak.fromStorageValue(previousStreak),
        strongestUrgeTime = UrgeTime.fromStorageValue(strongestUrgeTime),
        sleepSchedule = SleepSchedule.fromStorageValue(sleepSchedule),
        createdAtEpochMillis = createdAtEpochMillis,
    )

internal fun RecoveryPlanItem.toEntity(): RecoveryPlanItemEntity =
    RecoveryPlanItemEntity(
        code = code.storageValue,
        category = category.storageValue,
        orderIndex = orderIndex,
        isCompleted = isCompleted,
    )

internal fun RecoveryPlanItemEntity.toDomainItem(): RecoveryPlanItem? {
    val code = PlanItemCode.fromStorageValue(code) ?: return null
    return RecoveryPlanItem(
        code = code,
        category = PlanCategory.fromStorageValue(category),
        orderIndex = orderIndex,
        isCompleted = isCompleted,
    )
}

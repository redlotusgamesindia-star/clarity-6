package com.runtimelabs.clarity.data.repository

import com.runtimelabs.clarity.data.local.db.dao.RecoveryProfileDao
import com.runtimelabs.clarity.data.local.db.entity.RecoveryProfileEntity.Companion.LIST_SEPARATOR
import com.runtimelabs.clarity.domain.model.PlanCategory
import com.runtimelabs.clarity.domain.model.RecoveryPlan
import com.runtimelabs.clarity.domain.model.RecoveryProfile
import com.runtimelabs.clarity.domain.repository.RecoveryProfileRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Singleton
class RecoveryProfileRepositoryImpl @Inject constructor(
    private val dao: RecoveryProfileDao,
) : RecoveryProfileRepository {

    override val profile: Flow<RecoveryProfile?> =
        dao.observeProfile()
            .map { entity -> entity?.toDomainProfile() }
            .distinctUntilChanged()

    // Plan metadata (milestone, focus areas) lives on the profile row; items
    // live in their own table. Recombine into the domain aggregate here so
    // callers never see the storage split.
    override val plan: Flow<RecoveryPlan?> =
        combine(dao.observeProfile(), dao.observePlanItems()) { profileEntity, itemEntities ->
            if (profileEntity == null || itemEntities.isEmpty()) return@combine null
            RecoveryPlan(
                firstMilestoneDays = profileEntity.firstMilestoneDays,
                focusAreas = profileEntity.focusAreas
                    .split(LIST_SEPARATOR)
                    .filter { it.isNotBlank() }
                    .map { PlanCategory.fromStorageValue(it) },
                items = itemEntities
                    .mapNotNull { it.toDomainItem() }
                    .sortedBy { it.orderIndex },
            )
        }.distinctUntilChanged()

    override suspend fun saveProfileAndPlan(profile: RecoveryProfile, plan: RecoveryPlan) {
        dao.replaceProfileAndPlan(
            profile = profile.toEntity(plan),
            items = plan.items.map { it.toEntity() },
        )
    }
}

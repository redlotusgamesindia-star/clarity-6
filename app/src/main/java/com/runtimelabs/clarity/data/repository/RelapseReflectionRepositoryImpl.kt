package com.runtimelabs.clarity.data.repository

import com.runtimelabs.clarity.data.local.db.dao.RelapseReflectionDao
import com.runtimelabs.clarity.data.local.db.entity.RelapseReflectionEntity
import com.runtimelabs.clarity.domain.model.RelapseReflection
import com.runtimelabs.clarity.domain.repository.RelapseReflectionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RelapseReflectionRepositoryImpl @Inject constructor(
    private val dao: RelapseReflectionDao,
) : RelapseReflectionRepository {

    override suspend fun save(reflection: RelapseReflection) {
        dao.insert(
            RelapseReflectionEntity(
                journeyEventId = reflection.journeyEventId,
                epochDay = reflection.epochDay,
                createdAtEpochMillis = reflection.createdAtEpochMillis,
                trigger = reflection.trigger?.storageValue,
                timeOfDay = reflection.timeOfDay?.storageValue,
                mood = reflection.mood?.storageValue,
                location = reflection.location?.storageValue,
                notes = reflection.notes,
            ),
        )
    }
}

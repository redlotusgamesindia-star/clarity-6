package com.runtimelabs.clarity.data.repository

import com.runtimelabs.clarity.data.local.db.dao.JourneyDao
import com.runtimelabs.clarity.data.local.db.entity.JourneyEventEntity
import com.runtimelabs.clarity.domain.model.JourneyEvent
import com.runtimelabs.clarity.domain.model.JourneyEventType
import com.runtimelabs.clarity.domain.repository.JourneyRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Singleton
class JourneyRepositoryImpl @Inject constructor(
    private val dao: JourneyDao,
) : JourneyRepository {

    override fun observeEventDays(type: JourneyEventType): Flow<List<Long>> =
        dao.observeEventDays(type.storageValue).distinctUntilChanged()

    override suspend fun record(event: JourneyEvent) {
        dao.insert(
            JourneyEventEntity(
                type = event.type.storageValue,
                occurredAtEpochMillis = event.occurredAtEpochMillis,
                epochDay = event.epochDay,
            ),
        )
    }
}

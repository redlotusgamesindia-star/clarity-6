package com.runtimelabs.clarity.data.repository

import com.runtimelabs.clarity.data.local.db.dao.ThoughtRecordDao
import com.runtimelabs.clarity.data.local.db.entity.ThoughtRecordEntity
import com.runtimelabs.clarity.domain.model.ThoughtRecord
import com.runtimelabs.clarity.domain.repository.ThoughtRecordRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Singleton
class ThoughtRecordRepositoryImpl @Inject constructor(
    private val dao: ThoughtRecordDao,
) : ThoughtRecordRepository {

    override fun observeAll(): Flow<List<ThoughtRecord>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }.distinctUntilChanged()

    override suspend fun getById(id: Long): ThoughtRecord? = dao.getById(id)?.toDomain()

    override suspend fun save(record: ThoughtRecord): Long =
        if (record.id == ThoughtRecord.NEW_ID) {
            dao.insert(record.toEntity(id = 0))
        } else {
            dao.update(record.toEntity(id = record.id))
            record.id
        }

    override suspend fun delete(id: Long) = dao.deleteById(id)

    private fun ThoughtRecordEntity.toDomain() = ThoughtRecord(
        id = id,
        epochDay = epochDay,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
        situation = situation,
        automaticThought = automaticThought,
        feeling = feeling,
        feelingIntensity = feelingIntensity.coerceIn(0, 10),
        reframe = reframe,
    )

    private fun ThoughtRecord.toEntity(id: Long) = ThoughtRecordEntity(
        id = id,
        epochDay = epochDay,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
        situation = situation,
        automaticThought = automaticThought,
        feeling = feeling,
        feelingIntensity = feelingIntensity,
        reframe = reframe,
    )
}

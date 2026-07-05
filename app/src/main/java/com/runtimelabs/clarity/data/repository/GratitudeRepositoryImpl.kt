package com.runtimelabs.clarity.data.repository

import com.runtimelabs.clarity.data.local.db.dao.GratitudeDao
import com.runtimelabs.clarity.data.local.db.entity.GratitudeEntryEntity
import com.runtimelabs.clarity.domain.model.GratitudeEntry
import com.runtimelabs.clarity.domain.repository.GratitudeRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Singleton
class GratitudeRepositoryImpl @Inject constructor(
    private val dao: GratitudeDao,
) : GratitudeRepository {

    override fun observeAll(): Flow<List<GratitudeEntry>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }.distinctUntilChanged()

    override suspend fun getById(id: Long): GratitudeEntry? = dao.getById(id)?.toDomain()

    override suspend fun save(entry: GratitudeEntry): Long =
        if (entry.id == GratitudeEntry.NEW_ID) {
            dao.insert(entry.toEntity(id = 0))
        } else {
            dao.update(entry.toEntity(id = entry.id))
            entry.id
        }

    override suspend fun delete(id: Long) = dao.deleteById(id)

    private fun GratitudeEntryEntity.toDomain() = GratitudeEntry(
        id = id,
        epochDay = epochDay,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
        first = first,
        second = second,
        third = third,
    )

    private fun GratitudeEntry.toEntity(id: Long) = GratitudeEntryEntity(
        id = id,
        epochDay = epochDay,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
        first = first,
        second = second,
        third = third,
    )
}

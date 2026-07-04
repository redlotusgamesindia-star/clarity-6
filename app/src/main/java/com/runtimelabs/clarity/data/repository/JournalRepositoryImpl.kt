package com.runtimelabs.clarity.data.repository

import com.runtimelabs.clarity.data.local.db.dao.JournalDao
import com.runtimelabs.clarity.data.local.db.entity.JournalEntryEntity
import com.runtimelabs.clarity.domain.model.JournalEntry
import com.runtimelabs.clarity.domain.repository.JournalRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Singleton
class JournalRepositoryImpl @Inject constructor(
    private val dao: JournalDao,
) : JournalRepository {

    override fun observeAll(): Flow<List<JournalEntry>> =
        dao.observeAll()
            .map { rows -> rows.map { it.toDomain() } }
            .distinctUntilChanged()

    override suspend fun getById(id: Long): JournalEntry? = dao.getById(id)?.toDomain()

    override suspend fun save(entry: JournalEntry): Long =
        if (entry.id == JournalEntry.NEW_ID) {
            dao.insert(entry.toEntity(id = 0)) // 0 lets Room autogenerate
        } else {
            dao.update(entry.toEntity(id = entry.id))
            entry.id
        }

    override suspend fun delete(id: Long) = dao.deleteById(id)

    private fun JournalEntryEntity.toDomain() = JournalEntry(
        id = id,
        epochDay = epochDay,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
        body = body,
    )

    private fun JournalEntry.toEntity(id: Long) = JournalEntryEntity(
        id = id,
        epochDay = epochDay,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
        body = body,
    )
}

package com.runtimelabs.clarity.data.repository

import com.runtimelabs.clarity.data.local.db.dao.ToolkitUsageDao
import com.runtimelabs.clarity.data.local.db.entity.ToolkitUsageEntity
import com.runtimelabs.clarity.domain.repository.ToolkitUsageRepository
import com.runtimelabs.clarity.domain.toolkit.ToolkitTool
import com.runtimelabs.clarity.domain.toolkit.ToolkitUsageRecord
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ToolkitUsageRepositoryImpl @Inject constructor(
    private val dao: ToolkitUsageDao,
) : ToolkitUsageRepository {

    override fun observeAll(): Flow<List<ToolkitUsageRecord>> =
        dao.observeAll().map { entities ->
            entities.mapNotNull { entity ->
                val tool = ToolkitTool.fromStorageValue(entity.tool) ?: return@mapNotNull null
                ToolkitUsageRecord(
                    id = entity.id,
                    tool = tool,
                    startedAtEpochMillis = entity.startedAtEpochMillis,
                    durationSeconds = entity.durationSeconds,
                    epochDay = entity.epochDay,
                )
            }
        }

    override suspend fun record(tool: ToolkitTool, durationSeconds: Int) {
        dao.insert(
            ToolkitUsageEntity(
                tool = tool.storageValue,
                startedAtEpochMillis = System.currentTimeMillis(),
                durationSeconds = durationSeconds,
                epochDay = LocalDate.now().toEpochDay(),
            ),
        )
    }
}

package com.runtimelabs.clarity.domain.repository

import com.runtimelabs.clarity.domain.toolkit.ToolkitTool
import com.runtimelabs.clarity.domain.toolkit.ToolkitUsageRecord
import kotlinx.coroutines.flow.Flow

interface ToolkitUsageRepository {
    fun observeAll(): Flow<List<ToolkitUsageRecord>>
    suspend fun record(tool: ToolkitTool, durationSeconds: Int)
}

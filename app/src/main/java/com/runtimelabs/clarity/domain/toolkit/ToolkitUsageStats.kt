package com.runtimelabs.clarity.domain.toolkit

data class ToolkitUsageStats(
    val timesUsed: Int,
    val mostUsedTool: ToolkitTool?,
    val averageDurationSeconds: Int,
)

package com.runtimelabs.clarity.domain.toolkit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ToolkitUsageStatsCalculatorTest {

    private val calculator = ToolkitUsageStatsCalculator()

    private fun record(tool: ToolkitTool, durationSeconds: Int) = ToolkitUsageRecord(
        id = 0,
        tool = tool,
        startedAtEpochMillis = 0,
        durationSeconds = durationSeconds,
        epochDay = 100,
    )

    @Test
    fun `no records yields zeroed stats`() {
        val stats = calculator.compute(emptyList())
        assertEquals(0, stats.timesUsed)
        assertNull(stats.mostUsedTool)
        assertEquals(0, stats.averageDurationSeconds)
    }

    @Test
    fun `times used counts every record regardless of duration`() {
        val stats = calculator.compute(
            listOf(record(ToolkitTool.BREATHING_30S, 30), record(ToolkitTool.WRITE_JOURNAL, 0)),
        )
        assertEquals(2, stats.timesUsed)
    }

    @Test
    fun `most used tool is the one with the highest count`() {
        val stats = calculator.compute(
            listOf(
                record(ToolkitTool.GROUNDING, 60),
                record(ToolkitTool.GROUNDING, 45),
                record(ToolkitTool.BREATHING_30S, 30),
            ),
        )
        assertEquals(ToolkitTool.GROUNDING, stats.mostUsedTool)
    }

    @Test
    fun `a tie breaks toward the tool declared first in the enum, deterministically`() {
        val stats = calculator.compute(
            listOf(record(ToolkitTool.QUICK_REFRAME, 60), record(ToolkitTool.BREATHING_OPEN, 60)),
        )
        // BREATHING_OPEN is declared before QUICK_REFRAME in ToolkitTool —
        // ties must resolve the same way every time, not by map iteration order.
        assertEquals(ToolkitTool.BREATHING_OPEN, stats.mostUsedTool)
    }

    @Test
    fun `average duration excludes zero-duration records rather than counting them as zero`() {
        val stats = calculator.compute(
            listOf(
                record(ToolkitTool.BREATHING_30S, 30),
                record(ToolkitTool.BREATHING_30S, 60),
                record(ToolkitTool.WRITE_JOURNAL, 0),
            ),
        )
        // (30 + 60) / 2 = 45, NOT (30 + 60 + 0) / 3 = 30
        assertEquals(45, stats.averageDurationSeconds)
    }

    @Test
    fun `all zero-duration records yields a zero average, not a crash`() {
        val stats = calculator.compute(listOf(record(ToolkitTool.WRITE_JOURNAL, 0)))
        assertEquals(0, stats.averageDurationSeconds)
        assertEquals(1, stats.timesUsed)
    }

    @Test
    fun `deterministic for identical inputs`() {
        val records = listOf(record(ToolkitTool.WALK_OUTSIDE, 600), record(ToolkitTool.COLD_SHOWER, 30))
        assertEquals(calculator.compute(records), calculator.compute(records))
    }
}

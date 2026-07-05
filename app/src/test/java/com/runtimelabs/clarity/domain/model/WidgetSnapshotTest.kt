package com.runtimelabs.clarity.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetSnapshotTest {

    @Test
    fun `progress fraction is proportional midway`() {
        val snapshot = WidgetSnapshot(currentDays = 3, milestoneDays = 6)
        assertEquals(0.5f, snapshot.progressFraction, 0.0001f)
        assertFalse(snapshot.milestoneReached)
    }

    @Test
    fun `progress fraction clamps at one past the milestone`() {
        val snapshot = WidgetSnapshot(currentDays = 40, milestoneDays = 30)
        assertEquals(1f, snapshot.progressFraction, 0.0001f)
        assertTrue(snapshot.milestoneReached)
    }

    @Test
    fun `exactly at milestone counts as reached`() {
        val snapshot = WidgetSnapshot(currentDays = 7, milestoneDays = 7)
        assertEquals(1f, snapshot.progressFraction, 0.0001f)
        assertTrue(snapshot.milestoneReached)
    }

    @Test
    fun `zero milestone does not divide by zero`() {
        val snapshot = WidgetSnapshot(currentDays = 5, milestoneDays = 0)
        assertEquals(0f, snapshot.progressFraction, 0.0001f)
        assertFalse(snapshot.milestoneReached)
    }

    @Test
    fun `day zero is zero progress and not reached`() {
        val snapshot = WidgetSnapshot(currentDays = 0, milestoneDays = 7)
        assertEquals(0f, snapshot.progressFraction, 0.0001f)
        assertFalse(snapshot.milestoneReached)
    }
}

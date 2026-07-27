package com.runtimelabs.clarity.domain.badge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BadgeEvaluatorTest {

    private val evaluator = BadgeEvaluator()

    private fun stats(
        longestStreakDays: Int = 0,
        totalRelapses: Int = 0,
        hasMorningCheckIn: Boolean = false,
        journalEntryCount: Int = 0,
        longestToolkitUsageStreakDays: Int = 0,
    ) = BadgeStats(
        longestStreakDays = longestStreakDays,
        totalRelapses = totalRelapses,
        hasMorningCheckIn = hasMorningCheckIn,
        journalEntryCount = journalEntryCount,
        longestToolkitUsageStreakDays = longestToolkitUsageStreakDays,
    )

    @Test
    fun `zero everything unlocks nothing`() {
        assertEquals(emptyList<Badge>(), evaluator.evaluate(stats(), emptySet()))
    }

    @Test
    fun `streak ladder unlocks every threshold at or below the longest streak`() {
        val unlocked = evaluator.evaluate(stats(longestStreakDays = 21), emptySet())
        assertTrue(Badge.DAY_1 in unlocked)
        assertTrue(Badge.DAY_3 in unlocked)
        assertTrue(Badge.DAY_7 in unlocked)
        assertTrue(Badge.DAY_14 in unlocked)
        assertTrue(Badge.DAY_21 in unlocked)
        assertFalse(Badge.DAY_30 in unlocked)
        assertFalse(Badge.DAY_50 in unlocked)
        assertFalse(Badge.DAY_100 in unlocked)
        assertFalse(Badge.DAY_365 in unlocked)
    }

    @Test
    fun `streak badge unlocks exactly at its threshold, not one day early`() {
        assertTrue(Badge.DAY_7 in evaluator.evaluate(stats(longestStreakDays = 7), emptySet()))
        assertFalse(Badge.DAY_7 in evaluator.evaluate(stats(longestStreakDays = 6), emptySet()))
    }

    @Test
    fun `already unlocked badges are excluded even when still qualifying`() {
        val unlocked = evaluator.evaluate(
            stats(longestStreakDays = 7),
            alreadyUnlocked = setOf(Badge.DAY_1, Badge.DAY_3, Badge.DAY_7),
        )
        assertFalse(Badge.DAY_1 in unlocked)
        assertFalse(Badge.DAY_3 in unlocked)
        assertFalse(Badge.DAY_7 in unlocked)
    }

    @Test
    fun `first recovery needs one relapse, five recoveries needs five`() {
        assertFalse(Badge.FIRST_RECOVERY in evaluator.evaluate(stats(totalRelapses = 0), emptySet()))
        val one = evaluator.evaluate(stats(totalRelapses = 1), emptySet())
        assertTrue(Badge.FIRST_RECOVERY in one)
        assertFalse(Badge.FIVE_RECOVERIES in one)
        val four = evaluator.evaluate(stats(totalRelapses = 4), emptySet())
        assertFalse(Badge.FIVE_RECOVERIES in four)
        val five = evaluator.evaluate(stats(totalRelapses = 5), emptySet())
        assertTrue(Badge.FIRST_RECOVERY in five)
        assertTrue(Badge.FIVE_RECOVERIES in five)
    }

    @Test
    fun `morning check-in unlocks only when the flag is true`() {
        assertFalse(Badge.MORNING_CHECK_IN in evaluator.evaluate(stats(hasMorningCheckIn = false), emptySet()))
        assertTrue(Badge.MORNING_CHECK_IN in evaluator.evaluate(stats(hasMorningCheckIn = true), emptySet()))
    }

    @Test
    fun `journal writer needs five entries`() {
        assertFalse(Badge.JOURNAL_WRITER in evaluator.evaluate(stats(journalEntryCount = 4), emptySet()))
        assertTrue(Badge.JOURNAL_WRITER in evaluator.evaluate(stats(journalEntryCount = 5), emptySet()))
    }

    @Test
    fun `learning streak needs three consecutive toolkit-usage days`() {
        assertFalse(Badge.LEARNING_STREAK in evaluator.evaluate(stats(longestToolkitUsageStreakDays = 2), emptySet()))
        assertTrue(Badge.LEARNING_STREAK in evaluator.evaluate(stats(longestToolkitUsageStreakDays = 3), emptySet()))
    }

    @Test
    fun `every badge unlocks together when every stat clears its bar`() {
        val unlocked = evaluator.evaluate(
            stats(
                longestStreakDays = 365,
                totalRelapses = 5,
                hasMorningCheckIn = true,
                journalEntryCount = 5,
                longestToolkitUsageStreakDays = 3,
            ),
            emptySet(),
        )
        assertEquals(Badge.entries.size, unlocked.size)
        assertEquals(Badge.entries.toSet(), unlocked.toSet())
    }
}

class LongestConsecutiveRunTest {

    @Test
    fun `empty list is zero`() {
        assertEquals(0, longestConsecutiveRun(emptyList()))
    }

    @Test
    fun `single day is a run of one`() {
        assertEquals(1, longestConsecutiveRun(listOf(100L)))
    }

    @Test
    fun `consecutive days form one growing run`() {
        assertEquals(4, longestConsecutiveRun(listOf(10L, 11L, 12L, 13L)))
    }

    @Test
    fun `a gap starts a new run and only the longest counts`() {
        // 10-12 is a run of 3; 20-21 is a run of 2. Longest is 3.
        assertEquals(3, longestConsecutiveRun(listOf(10L, 11L, 12L, 20L, 21L)))
    }

    @Test
    fun `duplicate same-day entries collapse to one day`() {
        assertEquals(2, longestConsecutiveRun(listOf(5L, 5L, 5L, 6L)))
    }

    @Test
    fun `unsorted input is handled the same as sorted input`() {
        assertEquals(3, longestConsecutiveRun(listOf(12L, 10L, 11L)))
    }
}

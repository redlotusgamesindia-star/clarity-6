package com.runtimelabs.clarity.domain.recovery

import com.runtimelabs.clarity.domain.model.StreakSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComebackAchievementTest {

    // In every scenario below there's exactly one past relapse, so the
    // "previous" closed run and the "best" closed run are the same run —
    // bestClosedRunDays defaults to match previousRunDays for consistency
    // rather than an unrelated, easy-to-misread expression.
    private fun snapshot(
        currentDays: Int,
        longestDays: Int = currentDays,
        previousRunDays: Int? = null,
        totalRelapses: Int = 1,
        bestClosedRunDays: Int = previousRunDays ?: 0,
    ) = StreakSnapshot(
        currentDays = currentDays,
        longestDays = longestDays,
        cleanSinceEpochDay = 0,
        previousRunDays = previousRunDays,
        bestClosedRunDays = bestClosedRunDays,
        totalRelapses = totalRelapses,
        totalCleanDays = currentDays,
    )

    @Test
    fun `never relapsed unlocks nothing`() {
        val s = StreakSnapshot(100, 100, 0, null, 0, totalRelapses = 0, totalCleanDays = 100)
        assertEquals(emptyList<ComebackAchievement>(), s.unlockedComebackAchievements())
    }

    @Test
    fun `day one after a relapse unlocks only started again`() {
        val s = snapshot(currentDays = 1, previousRunDays = 40, totalRelapses = 1)
        assertEquals(listOf(ComebackAchievement.STARTED_AGAIN), s.unlockedComebackAchievements())
    }

    @Test
    fun `day three unlocks didnt quit too`() {
        val s = snapshot(currentDays = 3, previousRunDays = 40, totalRelapses = 1)
        val unlocked = s.unlockedComebackAchievements()
        assertTrue(ComebackAchievement.STARTED_AGAIN in unlocked)
        assertTrue(ComebackAchievement.DIDNT_QUIT in unlocked)
        assertTrue(ComebackAchievement.FIRST_WEEK_BACK !in unlocked)
    }

    @Test
    fun `day seven unlocks first week back`() {
        val s = snapshot(currentDays = 7, previousRunDays = 40, totalRelapses = 1)
        assertTrue(ComebackAchievement.FIRST_WEEK_BACK in s.unlockedComebackAchievements())
    }

    @Test
    fun `stronger than before needs half the previous run or a week, whichever is more`() {
        // Previous run was 10 days -> half is 5, but the 7-day floor wins.
        val shortPrevious = snapshot(currentDays = 6, previousRunDays = 10, totalRelapses = 1)
        assertTrue(ComebackAchievement.STRONGER_THAN_BEFORE !in shortPrevious.unlockedComebackAchievements())
        val shortPreviousAtFloor = snapshot(currentDays = 7, previousRunDays = 10, totalRelapses = 1)
        assertTrue(ComebackAchievement.STRONGER_THAN_BEFORE in shortPreviousAtFloor.unlockedComebackAchievements())

        // Previous run was 80 days -> half (40) exceeds the floor.
        val longPrevious = snapshot(currentDays = 39, previousRunDays = 80, totalRelapses = 1)
        assertTrue(ComebackAchievement.STRONGER_THAN_BEFORE !in longPrevious.unlockedComebackAchievements())
        val longPreviousAtHalf = snapshot(currentDays = 40, previousRunDays = 80, totalRelapses = 1)
        assertTrue(ComebackAchievement.STRONGER_THAN_BEFORE in longPreviousAtHalf.unlockedComebackAchievements())
    }

    @Test
    fun `beat previous record only when strictly exceeded`() {
        val s = StreakSnapshot(
            currentDays = 41, longestDays = 41, cleanSinceEpochDay = 0,
            previousRunDays = 40, bestClosedRunDays = 40, totalRelapses = 1, totalCleanDays = 41,
        )
        assertTrue(ComebackAchievement.BEAT_PREVIOUS_RECORD in s.unlockedComebackAchievements())

        val tie = s.copy(currentDays = 40, longestDays = 40)
        assertTrue(ComebackAchievement.BEAT_PREVIOUS_RECORD !in tie.unlockedComebackAchievements())
    }
}

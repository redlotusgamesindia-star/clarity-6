package com.runtimelabs.clarity.domain.recovery

import com.runtimelabs.clarity.domain.model.StreakSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Expected percentages below are independently verified against a plain
 * Python reimplementation of the formula before being written here, not
 * hand-computed in place — see the calculator's own doc comment for why
 * the weighting is shaped the way it is.
 */
class RecoveryScoreCalculatorTest {

    private val calculator = RecoveryScoreCalculator()

    private fun snapshot(
        currentDays: Int,
        longestDays: Int,
        totalRelapses: Int,
        totalCleanDays: Int,
    ) = StreakSnapshot(
        currentDays = currentDays,
        longestDays = longestDays,
        cleanSinceEpochDay = 0,
        previousRunDays = null,
        bestClosedRunDays = 0,
        totalRelapses = totalRelapses,
        totalCleanDays = totalCleanDays,
    )

    @Test
    fun `brand new perfect user scores honestly, not devastatingly low`() {
        val score = calculator.compute(snapshot(currentDays = 1, longestDays = 1, totalRelapses = 0, totalCleanDays = 1))
        assertEquals(61, score.percent)
        assertTrue("a new user with zero relapses should not score below half", score.percent >= 50)
    }

    @Test
    fun `never relapsed long streak scores a perfect 100`() {
        val score = calculator.compute(snapshot(150, 150, 0, 150))
        assertEquals(100, score.percent)
    }

    @Test
    fun `strong history keeps the score high right after a fresh relapse`() {
        // 12 prior relapses, 240 clean days, a 76-day best, but just restarted (day 1).
        val score = calculator.compute(snapshot(currentDays = 1, longestDays = 76, totalRelapses = 12, totalCleanDays = 240))
        assertEquals(75, score.percent)
        assertTrue("a strong lifetime history should not collapse after one new relapse", score.percent >= 70)
    }

    @Test
    fun `illustrative twelve-relapse scenario`() {
        val score = calculator.compute(snapshot(currentDays = 5, longestDays = 76, totalRelapses = 12, totalCleanDays = 241))
        assertEquals(77, score.percent)
        assertEquals(241, score.totalCleanDays)
        assertEquals(12, score.totalRelapses)
        assertEquals(76, score.bestStreakDays)
        assertEquals(5, score.currentStreakDays)
    }

    @Test
    fun `all-zero input does not crash and stays in range`() {
        val score = calculator.compute(snapshot(0, 0, 0, 0))
        assertTrue(score.percent in 0..100)
    }

    @Test
    fun `score never leaves the zero to one hundred range`() {
        val score = calculator.compute(snapshot(currentDays = 500, longestDays = 500, totalRelapses = 0, totalCleanDays = 500))
        assertTrue(score.percent in 0..100)
    }

    @Test
    fun `deterministic for identical inputs`() {
        val s = snapshot(20, 40, 3, 90)
        assertEquals(calculator.compute(s), calculator.compute(s))
    }
}

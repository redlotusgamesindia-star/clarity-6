package com.runtimelabs.clarity.domain.streak

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the streak semantics documented on the calculator: day-after-relapse
 * restarts, Day-1-on-start, inclusive today, closed runs exclude the relapse
 * day. Days are plain Longs so scenarios read as arithmetic. Extended in
 * Phase E for previousRunDays / bestClosedRunDays / totalRelapses /
 * totalCleanDays — the numbers the Rebuild System, Recovery Score, and
 * comeback achievements are all built on.
 */
class StreakCalculatorTest {

    private val calculator = StreakCalculator()

    @Test
    fun `start day counts as day one`() {
        val s = calculator.compute(recoveryStartEpochDay = 100, relapseEpochDays = emptyList(), todayEpochDay = 100)
        assertEquals(1, s.currentDays)
        assertEquals(1, s.longestDays)
        assertEquals(100, s.cleanSinceEpochDay)
        assertNull(s.previousRunDays)
        assertEquals(0, s.bestClosedRunDays)
        assertEquals(0, s.totalRelapses)
        assertEquals(1, s.totalCleanDays)
    }

    @Test
    fun `clean days accumulate inclusively`() {
        val s = calculator.compute(100, emptyList(), todayEpochDay = 106)
        assertEquals(7, s.currentDays)
        assertEquals(7, s.longestDays)
        assertEquals(7, s.totalCleanDays)
    }

    @Test
    fun `relapse today reads zero`() {
        val s = calculator.compute(100, listOf(110), todayEpochDay = 110)
        assertEquals(0, s.currentDays)
        assertEquals(111, s.cleanSinceEpochDay) // clean run begins tomorrow
        assertEquals(10, s.previousRunDays) // 100..109
        assertEquals(1, s.totalRelapses)
    }

    @Test
    fun `day after relapse reads day one`() {
        val s = calculator.compute(100, listOf(110), todayEpochDay = 111)
        assertEquals(1, s.currentDays)
    }

    @Test
    fun `closed run excludes the relapse day`() {
        // Clean 100..109 (10 days), relapse on 110.
        val s = calculator.compute(100, listOf(110), todayEpochDay = 120)
        assertEquals(10, s.currentDays) // 111..120 inclusive
        assertEquals(10, s.longestDays) // tie: current equals best
        assertEquals(10, s.bestClosedRunDays)
    }

    @Test
    fun `matching but not exceeding the old record does not count as beaten`() {
        val s = calculator.compute(100, listOf(110), todayEpochDay = 120) // current == previous closed run == 10
        assertEquals(s.bestClosedRunDays, s.currentDays)
        assertFalse(s.hasBeatenPreviousRecord) // equal is not "beaten"
        assertTrue(s.isRebuilding)
    }

    @Test
    fun `one day past the old record counts as beaten`() {
        val s = calculator.compute(100, listOf(110), todayEpochDay = 121) // current = 11 > bestClosedRun(10)
        assertTrue(s.hasBeatenPreviousRecord)
        assertFalse(s.isRebuilding)
    }

    @Test
    fun `longest survives across multiple relapses`() {
        // Runs: 100..119 (20), relapse 120; 121..125 (5), relapse 126; current from 127.
        val s = calculator.compute(100, listOf(120, 126), todayEpochDay = 130)
        assertEquals(4, s.currentDays)   // 127..130
        assertEquals(20, s.longestDays)
        assertEquals(127, s.cleanSinceEpochDay)
        assertEquals(20, s.bestClosedRunDays)
        assertEquals(5, s.previousRunDays) // the run that just ended (121..125), NOT the best (20)
        assertEquals(2, s.totalRelapses)
        assertEquals(29, s.totalCleanDays) // 20 + 5 + 4
    }

    @Test
    fun `current run can become the longest`() {
        val s = calculator.compute(100, listOf(102), todayEpochDay = 200)
        assertEquals(98, s.currentDays)  // 103..200
        assertEquals(98, s.longestDays)
        assertEquals(2, s.bestClosedRunDays)   // 100..101
        assertTrue(s.hasBeatenPreviousRecord)
    }

    @Test
    fun `unsorted and duplicate relapse days are normalized`() {
        val a = calculator.compute(100, listOf(126, 120, 126, 120), todayEpochDay = 130)
        val b = calculator.compute(100, listOf(120, 126), todayEpochDay = 130)
        assertEquals(b, a)
    }

    @Test
    fun `out of range relapse days are ignored`() {
        // One before recovery start, one in the future: both data errors, both dropped.
        val s = calculator.compute(100, listOf(50, 999), todayEpochDay = 105)
        assertEquals(6, s.currentDays)
        assertEquals(6, s.longestDays)
        assertEquals(0, s.totalRelapses)
        assertNull(s.previousRunDays)
    }

    @Test
    fun `consecutive-day relapses produce a zero-length run between them`() {
        val s = calculator.compute(100, listOf(105, 106), todayEpochDay = 108)
        assertEquals(2, s.currentDays)   // 107..108
        assertEquals(5, s.longestDays)   // 100..104
        assertEquals(0, s.previousRunDays) // 105..104 is a zero-length run (relapsed the day after relapsing)
        assertEquals(5, s.bestClosedRunDays)
        assertEquals(7, s.totalCleanDays) // 5 + 0 + 2
    }

    @Test
    fun `never relapsed means no rebuilding and no beaten record`() {
        val s = calculator.compute(100, emptyList(), todayEpochDay = 150)
        assertFalse(s.isRebuilding)
        assertFalse(s.hasBeatenPreviousRecord)
        assertNull(s.previousRunDays)
    }

    @Test
    fun `deterministic for identical inputs`() {
        val x = calculator.compute(100, listOf(120, 126), 130)
        val y = calculator.compute(100, listOf(120, 126), 130)
        assertEquals(x, y)
    }
}

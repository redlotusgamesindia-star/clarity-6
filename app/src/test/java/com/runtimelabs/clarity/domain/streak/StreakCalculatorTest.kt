package com.runtimelabs.clarity.domain.streak

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pins the streak semantics documented on the calculator: day-after-relapse
 * restarts, Day-1-on-start, inclusive today, closed runs exclude the relapse
 * day. Days are plain Longs so scenarios read as arithmetic.
 */
class StreakCalculatorTest {

    private val calculator = StreakCalculator()

    @Test
    fun `start day counts as day one`() {
        val s = calculator.compute(recoveryStartEpochDay = 100, relapseEpochDays = emptyList(), todayEpochDay = 100)
        assertEquals(1, s.currentDays)
        assertEquals(1, s.longestDays)
        assertEquals(100, s.cleanSinceEpochDay)
    }

    @Test
    fun `clean days accumulate inclusively`() {
        val s = calculator.compute(100, emptyList(), todayEpochDay = 106)
        assertEquals(7, s.currentDays)
        assertEquals(7, s.longestDays)
    }

    @Test
    fun `relapse today reads zero`() {
        val s = calculator.compute(100, listOf(110), todayEpochDay = 110)
        assertEquals(0, s.currentDays)
        assertEquals(111, s.cleanSinceEpochDay) // clean run begins tomorrow
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
        assertEquals(10, s.longestDays.coerceAtLeast(10)) // closed run was 10
        assertEquals(10, s.currentDays) // 111..120 inclusive
        assertEquals(10, s.longestDays) // tie: current equals best
    }

    @Test
    fun `longest survives across multiple relapses`() {
        // Runs: 100..119 (20), relapse 120; 121..125 (5), relapse 126; current from 127.
        val s = calculator.compute(100, listOf(120, 126), todayEpochDay = 130)
        assertEquals(4, s.currentDays)   // 127..130
        assertEquals(20, s.longestDays)
        assertEquals(127, s.cleanSinceEpochDay)
    }

    @Test
    fun `current run can become the longest`() {
        val s = calculator.compute(100, listOf(102), todayEpochDay = 200)
        assertEquals(98, s.currentDays)  // 103..200
        assertEquals(98, s.longestDays)
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
    }

    @Test
    fun `consecutive-day relapses produce a zero-length run between them`() {
        val s = calculator.compute(100, listOf(105, 106), todayEpochDay = 108)
        assertEquals(2, s.currentDays)   // 107..108
        assertEquals(5, s.longestDays)   // 100..104
    }

    @Test
    fun `deterministic for identical inputs`() {
        val x = calculator.compute(100, listOf(120, 126), 130)
        val y = calculator.compute(100, listOf(120, 126), 130)
        assertEquals(x, y)
    }
}

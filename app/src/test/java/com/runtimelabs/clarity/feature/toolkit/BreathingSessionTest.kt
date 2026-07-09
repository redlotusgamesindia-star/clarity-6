package com.runtimelabs.clarity.feature.toolkit

import org.junit.Assert.assertEquals
import org.junit.Test

class BreathingSessionTest {

    private fun advanceBy(state: BreathingSessionState, seconds: Int): BreathingSessionState {
        var s = state
        repeat(seconds) { s = advanceOneSecond(s) }
        return s
    }

    @Test
    fun `counts down within a phase`() {
        val s = advanceOneSecond(BreathingSessionState(BreathingPatterns.CALM))
        assertEquals(0, s.phaseIndex)
        assertEquals(3, s.secondsRemainingInPhase)
        assertEquals(1, s.elapsedSeconds)
    }

    @Test
    fun `advances to the next phase at zero`() {
        val s = advanceBy(BreathingSessionState(BreathingPatterns.CALM), 4)
        assertEquals(BreathPhaseKind.EXHALE, s.currentPhase.kind)
        assertEquals(6, s.secondsRemainingInPhase)
        assertEquals(0, s.completedCycles)
    }

    @Test
    fun `wrapping the last phase completes a cycle`() {
        val s = advanceBy(BreathingSessionState(BreathingPatterns.CALM), 10) // 4 + 6
        assertEquals(BreathPhaseKind.INHALE, s.currentPhase.kind)
        assertEquals(4, s.secondsRemainingInPhase)
        assertEquals(1, s.completedCycles)
        assertEquals(10, s.elapsedSeconds)
    }

    @Test
    fun `box pattern walks all four phases in order`() {
        var s = BreathingSessionState(BreathingPatterns.BOX)
        val seen = mutableListOf(s.currentPhase.kind)
        repeat(16) {
            s = advanceOneSecond(s)
            if (seen.last() != s.currentPhase.kind || s.completedCycles > 0 && seen.size == 4) Unit
            if (seen.last() != s.currentPhase.kind) seen += s.currentPhase.kind
        }
        assertEquals(
            listOf(
                BreathPhaseKind.INHALE,
                BreathPhaseKind.HOLD_IN,
                BreathPhaseKind.EXHALE,
                BreathPhaseKind.HOLD_OUT,
                BreathPhaseKind.INHALE,
            ),
            seen.take(5),
        )
        assertEquals(1, s.completedCycles)
    }

    @Test
    fun `unknown pattern code falls back to calm`() {
        assertEquals(BreathingPatterns.CALM, BreathingPatterns.byCode("nope"))
    }

    @Test
    fun `three full calm cycles`() {
        val s = advanceBy(BreathingSessionState(BreathingPatterns.CALM), 30)
        assertEquals(3, s.completedCycles)
    }

    @Test
    fun `open-ended session never reaches a target`() {
        val s = advanceBy(BreathingSessionState(BreathingPatterns.CALM, targetDurationSeconds = 0), 300)
        assertEquals(false, s.hasReachedTarget)
    }

    @Test
    fun `a timed session has not reached target before elapsed time catches up`() {
        val s = advanceBy(BreathingSessionState(BreathingPatterns.CALM, targetDurationSeconds = 30), 29)
        assertEquals(false, s.hasReachedTarget)
    }

    @Test
    fun `a timed session reaches target exactly on the boundary second`() {
        val s = advanceBy(BreathingSessionState(BreathingPatterns.CALM, targetDurationSeconds = 30), 30)
        assertEquals(true, s.hasReachedTarget)
    }

    @Test
    fun `a timed session stays reached past the boundary`() {
        val s = advanceBy(BreathingSessionState(BreathingPatterns.CALM, targetDurationSeconds = 30), 45)
        assertEquals(true, s.hasReachedTarget)
    }
}

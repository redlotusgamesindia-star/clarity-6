package com.runtimelabs.clarity.domain.recovery

import com.runtimelabs.clarity.domain.model.MainTrigger
import com.runtimelabs.clarity.domain.model.UrgeTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryChecklistGeneratorTest {

    private val generator = RecoveryChecklistGenerator()

    private fun codes(trigger: MainTrigger?, time: UrgeTime?) =
        generator.generate(trigger, time).map { it.code }

    @Test
    fun `no reflection still yields the five foundations`() {
        val result = codes(null, null)
        assertEquals(
            setOf(
                RecoveryChecklistItemCode.DRINK_WATER,
                RecoveryChecklistItemCode.TAKE_WALK,
                RecoveryChecklistItemCode.SHOWER,
                RecoveryChecklistItemCode.BREATHING_EXERCISE,
                RecoveryChecklistItemCode.JOURNAL_IT,
            ),
            result.toSet(),
        )
        assertEquals(5, result.size)
    }

    @Test
    fun `loneliness adds reach out`() {
        assertTrue(RecoveryChecklistItemCode.REACH_OUT in codes(MainTrigger.LONELINESS, null))
        assertTrue(RecoveryChecklistItemCode.REACH_OUT !in codes(MainTrigger.STRESS, null))
    }

    @Test
    fun `boredom adds plan next hour`() {
        assertTrue(RecoveryChecklistItemCode.PLAN_NEXT_HOUR in codes(MainTrigger.BOREDOM, null))
    }

    @Test
    fun `late night adds wind down`() {
        assertTrue(RecoveryChecklistItemCode.WIND_DOWN in codes(null, UrgeTime.LATE_NIGHT))
        assertTrue(RecoveryChecklistItemCode.WIND_DOWN !in codes(null, UrgeTime.MORNING))
    }

    @Test
    fun `trigger and time personalizations stack`() {
        val result = codes(MainTrigger.LONELINESS, UrgeTime.LATE_NIGHT)
        assertEquals(7, result.size) // 5 foundations + reach out + wind down
    }

    @Test
    fun `no duplicate codes ever`() {
        val result = codes(MainTrigger.BOREDOM, UrgeTime.LATE_NIGHT)
        assertEquals(result.size, result.toSet().size)
    }

    @Test
    fun `deterministic for identical inputs`() {
        assertEquals(
            generator.generate(MainTrigger.STRESS, UrgeTime.EVENING),
            generator.generate(MainTrigger.STRESS, UrgeTime.EVENING),
        )
    }
}

package com.runtimelabs.clarity.domain.recovery

import com.runtimelabs.clarity.domain.model.RelapseTrigger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryChecklistGeneratorTest {

    private val generator = RecoveryChecklistGenerator()

    private fun codes(trigger: RelapseTrigger?) = generator.generate(trigger).map { it.code }

    @Test
    fun `no trigger still yields the five foundations`() {
        val result = codes(null)
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
        assertTrue(RecoveryChecklistItemCode.REACH_OUT in codes(RelapseTrigger.LONELINESS))
        assertTrue(RecoveryChecklistItemCode.REACH_OUT !in codes(RelapseTrigger.STRESS))
    }

    @Test
    fun `boredom adds plan next hour`() {
        assertTrue(RecoveryChecklistItemCode.PLAN_NEXT_HOUR in codes(RelapseTrigger.BOREDOM))
    }

    @Test
    fun `night and couldnt sleep both add wind down`() {
        assertTrue(RecoveryChecklistItemCode.WIND_DOWN in codes(RelapseTrigger.NIGHT))
        assertTrue(RecoveryChecklistItemCode.WIND_DOWN in codes(RelapseTrigger.COULDNT_SLEEP))
        assertTrue(RecoveryChecklistItemCode.WIND_DOWN !in codes(RelapseTrigger.STRESS))
    }

    @Test
    fun `stress, social media, and other add no personalized item`() {
        assertEquals(5, codes(RelapseTrigger.STRESS).size)
        assertEquals(5, codes(RelapseTrigger.SOCIAL_MEDIA).size)
        assertEquals(5, codes(RelapseTrigger.OTHER).size)
    }

    @Test
    fun `no duplicate codes ever`() {
        val result = codes(RelapseTrigger.NIGHT)
        assertEquals(result.size, result.toSet().size)
    }

    @Test
    fun `deterministic for identical inputs`() {
        assertEquals(generator.generate(RelapseTrigger.STRESS), generator.generate(RelapseTrigger.STRESS))
    }
}

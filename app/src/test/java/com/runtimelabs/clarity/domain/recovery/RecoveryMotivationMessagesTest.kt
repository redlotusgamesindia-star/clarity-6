package com.runtimelabs.clarity.domain.recovery

import org.junit.Assert.assertEquals
import org.junit.Test

class RecoveryMotivationMessagesTest {

    @Test
    fun `days one through seven map to distinct exact-day codes`() {
        val expected = listOf(
            RecoveryMotivationCode.DAY_1, RecoveryMotivationCode.DAY_2, RecoveryMotivationCode.DAY_3,
            RecoveryMotivationCode.DAY_4, RecoveryMotivationCode.DAY_5, RecoveryMotivationCode.DAY_6,
            RecoveryMotivationCode.DAY_7,
        )
        val actual = (1..7).map { RecoveryMotivationMessages.forDay(it) }
        assertEquals(expected, actual)
    }

    @Test
    fun `weekly milestones map correctly`() {
        assertEquals(RecoveryMotivationCode.WEEK_2, RecoveryMotivationMessages.forDay(14))
        assertEquals(RecoveryMotivationCode.WEEK_3, RecoveryMotivationMessages.forDay(21))
        assertEquals(RecoveryMotivationCode.MONTH_1, RecoveryMotivationMessages.forDay(30))
    }

    @Test
    fun `unmapped days fall back to ongoing`() {
        assertEquals(RecoveryMotivationCode.ONGOING, RecoveryMotivationMessages.forDay(8))
        assertEquals(RecoveryMotivationCode.ONGOING, RecoveryMotivationMessages.forDay(13))
        assertEquals(RecoveryMotivationCode.ONGOING, RecoveryMotivationMessages.forDay(365))
    }

    @Test
    fun `deterministic for identical inputs`() {
        assertEquals(RecoveryMotivationMessages.forDay(5), RecoveryMotivationMessages.forDay(5))
    }
}

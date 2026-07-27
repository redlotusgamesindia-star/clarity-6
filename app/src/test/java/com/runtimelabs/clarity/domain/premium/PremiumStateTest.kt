package com.runtimelabs.clarity.domain.premium

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumStateTest {

    @Test
    fun `true maps to Premium`() {
        assertEquals(PremiumState.Premium, premiumStateOf(true))
    }

    @Test
    fun `false maps to Free`() {
        assertEquals(PremiumState.Free, premiumStateOf(false))
    }

    @Test
    fun `isPremium extension matches the sealed case`() {
        assertTrue(PremiumState.Premium.isPremium)
        assertFalse(PremiumState.Free.isPremium)
    }
}

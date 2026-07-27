package com.runtimelabs.clarity.domain.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdPolicyTest {

    @Test
    fun `every allow-listed screen shows ads for non-premium users`() {
        for (screen in AdScreen.entries) {
            assertTrue(screen.name, AdPolicy.isAdsAllowed(screen, isPremiumUser = false))
        }
    }

    @Test
    fun `premium users never see ads regardless of screen`() {
        for (screen in AdScreen.entries) {
            assertFalse(screen.name, AdPolicy.isAdsAllowed(screen, isPremiumUser = true))
        }
    }

    @Test
    fun `debug builds always resolve to the test ad unit id`() {
        val result = AdPolicy.selectBannerAdUnitId(
            isDebugBuild = true,
            testAdUnitId = "test-id",
            productionAdUnitId = "prod-id",
        )
        assertEquals("test-id", result)
    }

    @Test
    fun `release builds always resolve to the production ad unit id`() {
        val result = AdPolicy.selectBannerAdUnitId(
            isDebugBuild = false,
            testAdUnitId = "test-id",
            productionAdUnitId = "prod-id",
        )
        assertEquals("prod-id", result)
    }

    @Test
    fun `allow-list contains exactly the five specified screens, nothing more`() {
        assertEquals(
            setOf(AdScreen.HOME, AdScreen.JOURNEY, AdScreen.LEARN, AdScreen.JOURNAL_LIST, AdScreen.SETTINGS),
            AdScreen.entries.toSet(),
        )
    }
}

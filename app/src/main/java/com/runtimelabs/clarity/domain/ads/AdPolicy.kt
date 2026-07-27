package com.runtimelabs.clarity.domain.ads

/**
 * Pure ad-gating rules — no Android or AdMob types, so this is testable
 * without the SDK, an emulator, or a network connection (AD-4: explainable,
 * deterministic). [AdsManager][com.runtimelabs.clarity.ads.AdsManager]
 * consults this; it contains no policy logic of its own.
 */
object AdPolicy {

    /**
     * [screen] is deliberately part of the signature even though every
     * current [AdScreen] resolves the same way — it documents intent at the
     * call site ("is showing an ad here allowed") and gives a future
     * per-screen exception somewhere to live without changing the shape of
     * every caller.
     */
    fun isAdsAllowed(screen: AdScreen, isPremiumUser: Boolean): Boolean = !isPremiumUser

    /** Debug builds always see Google's test creative; only release builds request real ads. */
    fun selectBannerAdUnitId(
        isDebugBuild: Boolean,
        testAdUnitId: String,
        productionAdUnitId: String,
    ): String = if (isDebugBuild) testAdUnitId else productionAdUnitId
}

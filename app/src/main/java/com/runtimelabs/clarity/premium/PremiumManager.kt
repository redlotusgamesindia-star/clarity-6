package com.runtimelabs.clarity.premium

import com.runtimelabs.clarity.domain.premium.BillingConnector
import com.runtimelabs.clarity.domain.premium.PremiumRepository
import com.runtimelabs.clarity.domain.premium.PremiumState
import com.runtimelabs.clarity.domain.premium.isPremium
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The single global source of premium status. Every other class in the app
 * — [com.runtimelabs.clarity.ads.BannerAdViewModel], the Settings screen,
 * anything else that ever needs to ask "is this person premium" — depends
 * on this, never on [PremiumRepository] or [BillingConnector] directly.
 * This is the one property that answers "hide every ad" (plan requirement):
 * flip what this reports true/false for, and every
 * [ClarityBannerAd][com.runtimelabs.clarity.ads.ClarityBannerAd] in the app
 * reacts, because they all already gate on it through
 * [AdPolicy][com.runtimelabs.clarity.domain.ads.AdPolicy].
 *
 * [premiumState] itself is a thin passthrough of [PremiumRepository] — the
 * cached, durable-across-restarts value everything reads reactively.
 * [BillingConnector] is genuinely injected and callable via
 * [refreshFromBilling], not just bound-and-unused: this is the one concrete
 * method a future purchase/restore flow would call to reconcile a live
 * entitlement check into the cached value, without needing to change
 * anything that already reads [premiumState]. Nothing calls it yet, since
 * [BillingConnector]'s only implementation is a no-op — but the seam is
 * real, not aspirational.
 */
@Singleton
class PremiumManager @Inject constructor(
    private val premiumRepository: PremiumRepository,
    private val billingConnector: BillingConnector,
) {
    val premiumState: Flow<PremiumState> = premiumRepository.premiumState

    val isPremium: Flow<Boolean> = premiumState.map { it.isPremium }

    /**
     * Queries [billingConnector] for the live entitlement and persists it
     * via [PremiumRepository], so every [premiumState] observer picks up
     * the change automatically. The future purchase/restore flow's landing
     * spot; unused until that exists.
     */
    suspend fun refreshFromBilling() {
        val liveState = billingConnector.queryEntitlement()
        premiumRepository.setPremiumState(liveState)
    }
}

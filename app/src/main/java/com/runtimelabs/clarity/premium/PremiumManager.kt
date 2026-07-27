package com.runtimelabs.clarity.premium

import android.app.Activity
import com.runtimelabs.clarity.domain.premium.BillingConnector
import com.runtimelabs.clarity.domain.premium.PremiumRepository
import com.runtimelabs.clarity.domain.premium.PremiumState
import com.runtimelabs.clarity.domain.premium.PurchaseResult
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
 * [purchasePremium] and [restorePurchases] both persist a successful (or
 * pending) outcome back through [PremiumRepository] the moment it's known,
 * which is the entire mechanism behind "remove every banner instantly" —
 * nothing downstream needs to be told to refresh; it already observes this.
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
     * the change automatically. Called at app startup ([com.runtimelabs.clarity.ClarityApp])
     * so a purchase made before a reinstall — still valid on Google's
     * servers even though nothing survived locally — is rediscovered the
     * moment billing successfully connects, with no user action required.
     */
    suspend fun refreshFromBilling() {
        val liveState = billingConnector.queryEntitlement()
        premiumRepository.setPremiumState(liveState)
    }

    /** Launches the purchase flow for the remove-ads product and persists a successful or pending outcome. */
    suspend fun purchasePremium(activity: Activity): PurchaseResult {
        val result = billingConnector.purchasePremium(activity)
        persistIfOwnershipChanged(result)
        return result
    }

    /** Re-checks Google's records for a past purchase — the mechanism behind "survives reinstall". */
    suspend fun restorePurchases(): PurchaseResult {
        val result = billingConnector.restorePurchases()
        persistIfOwnershipChanged(result)
        return result
    }

    /** Cancelled/error/unavailable outcomes never touch the persisted state — only a genuine change in ownership does. */
    private suspend fun persistIfOwnershipChanged(result: PurchaseResult) {
        when (result) {
            PurchaseResult.Success -> premiumRepository.setPremiumState(PremiumState.Premium)
            PurchaseResult.Pending -> premiumRepository.setPremiumState(PremiumState.Pending)
            PurchaseResult.Cancelled,
            PurchaseResult.BillingUnavailable,
            PurchaseResult.NothingToRestore,
            is PurchaseResult.Error,
            -> Unit
        }
    }
}

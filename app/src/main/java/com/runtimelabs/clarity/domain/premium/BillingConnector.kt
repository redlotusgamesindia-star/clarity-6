package com.runtimelabs.clarity.domain.premium

/**
 * The seam Google Play Billing plugs into later. No Play Billing types
 * appear in this signature on purpose — the same "domain stays platform-
 * free even though the only consumer is a platform surface" reasoning as
 * [com.runtimelabs.clarity.domain.repository.WidgetSyncRepository].
 *
 * [com.runtimelabs.clarity.data.premium.NoOpBillingConnector] is the only
 * implementation today: it always reports [PremiumState.Free] and no-ops
 * on purchase/restore. A real `PlayBillingConnector` can replace it later
 * with zero change to anything that depends on this interface.
 */
interface BillingConnector {

    /** Queries the current live entitlement. Never implies a purchase happened. */
    suspend fun queryEntitlement(): PremiumState

    /** Starts a purchase flow for the premium (remove-ads) product. */
    suspend fun purchasePremium(): PremiumState

    /** Re-checks past purchases (e.g. after a reinstall or device change). */
    suspend fun restorePurchases(): PremiumState
}

package com.runtimelabs.clarity.domain.premium

import android.app.Activity

/**
 * The seam Google Play Billing plugs into. No Play-Billing-specific types
 * (`BillingClient`, `Purchase`, etc.) appear in this signature — the same
 * "domain stays platform-free even though the only consumer is a platform
 * surface" reasoning as [com.runtimelabs.clarity.domain.repository.WidgetSyncRepository].
 *
 * [Activity] is the one deliberate exception, on [purchasePremium] only.
 * This is not a design choice being smuggled in — Android's own billing
 * flow API fundamentally requires an Activity to launch Play's purchase
 * UI; there is no way to call it without one. [queryEntitlement] and
 * [restorePurchases] need no UI at all (they're background queries against
 * Play's records), so they stay Activity-free.
 *
 * [com.runtimelabs.clarity.data.premium.PlayBillingConnector] is the real,
 * production implementation, bound via Hilt.
 */
interface BillingConnector {

    /** Queries the current live entitlement. Never implies a purchase happened. */
    suspend fun queryEntitlement(): PremiumState

    /** Launches Play's purchase UI for the remove-ads product and awaits its outcome. */
    suspend fun purchasePremium(activity: Activity): PurchaseResult

    /** Re-checks past purchases against Play's records (e.g. after a reinstall or device change). */
    suspend fun restorePurchases(): PurchaseResult
}

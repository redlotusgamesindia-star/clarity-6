package com.runtimelabs.clarity.data.premium

import com.runtimelabs.clarity.domain.premium.BillingConnector
import com.runtimelabs.clarity.domain.premium.PremiumState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The only [BillingConnector] implementation today. Every method is
 * honestly inert — no Play Billing dependency exists in this project yet.
 * This exists so the interface is genuinely wired end-to-end (Hilt has a
 * real binding, [PremiumManager][com.runtimelabs.clarity.premium.PremiumManager]
 * has something real to call) rather than a half-built abstraction nothing
 * actually implements. Swapping in a real `PlayBillingConnector` later is a
 * one-class, one-binding change.
 */
@Singleton
class NoOpBillingConnector @Inject constructor() : BillingConnector {

    override suspend fun queryEntitlement(): PremiumState = PremiumState.Free

    override suspend fun purchasePremium(): PremiumState = PremiumState.Free

    override suspend fun restorePurchases(): PremiumState = PremiumState.Free
}

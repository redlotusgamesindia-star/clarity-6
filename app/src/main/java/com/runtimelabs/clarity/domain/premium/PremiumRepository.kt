package com.runtimelabs.clarity.domain.premium

import kotlinx.coroutines.flow.Flow

/**
 * The cached/persisted premium flag — durable across process death, backed
 * by DataStore ([com.runtimelabs.clarity.data.local.datastore.PremiumPreferences]).
 * This is deliberately NOT where billing logic lives; [BillingConnector] is
 * that seam. This repository only remembers what was last known to be true.
 */
interface PremiumRepository {

    val premiumState: Flow<PremiumState>

    /**
     * Persists a new entitlement result. Nothing calls this yet — it exists
     * so the future billing layer (via [BillingConnector] and whatever
     * coordinates it) has a durable place to write a purchase result to,
     * without needing to change this interface when that lands.
     */
    suspend fun setPremiumState(state: PremiumState)
}

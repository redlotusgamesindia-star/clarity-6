package com.runtimelabs.clarity.domain.premium

/**
 * Not a raw Boolean on purpose. Today the app only distinguishes Free vs
 * Premium, but this is exactly the kind of flag that needs to grow later
 * (a trial period, an expiring subscription, a pending purchase) — a sealed
 * type lets every future case get added and pattern-matched without
 * touching a single existing consumer, the same reasoning [RecoveryGoal]
 * and every other closed-but-growable domain enum in this app already
 * follows.
 */
sealed interface PremiumState {
    data object Free : PremiumState
    data object Premium : PremiumState
}

/** Pure mapping, tested without DataStore/Context — the boundary where raw storage meets the domain type. */
fun premiumStateOf(isPremium: Boolean): PremiumState =
    if (isPremium) PremiumState.Premium else PremiumState.Free

val PremiumState.isPremium: Boolean get() = this is PremiumState.Premium

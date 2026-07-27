package com.runtimelabs.clarity.domain.premium

/**
 * Not a raw Boolean on purpose. [Pending] is exactly the case this sealed
 * type was built to absorb without touching every existing consumer: some
 * payment methods (bank transfers, cash-at-convenience-store flows common
 * in Japan, Germany, Brazil, Mexico, Indonesia) take time to clear, and a
 * pending purchase must not unlock premium yet — see [isPremium] below,
 * which is deliberately true for [Premium] alone.
 */
sealed interface PremiumState {
    data object Free : PremiumState
    data object Premium : PremiumState
    data object Pending : PremiumState
}

/** Pure mapping, tested without DataStore/Context — the boundary where raw storage meets the domain type. */
fun premiumStateOf(isPremium: Boolean): PremiumState =
    if (isPremium) PremiumState.Premium else PremiumState.Free

/** Only a confirmed Premium purchase hides ads — a Pending one must not, until it actually clears. */
val PremiumState.isPremium: Boolean get() = this is PremiumState.Premium

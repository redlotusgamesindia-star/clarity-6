package com.runtimelabs.clarity.domain.premium

/**
 * The outcome of one specific purchase or restore attempt — distinct from
 * [PremiumState], which is durable ownership status. Cancelling a purchase
 * doesn't change what you own; it's just an event this attempt produced,
 * which is exactly why it doesn't belong as a [PremiumState] case.
 */
sealed interface PurchaseResult {
    data object Success : PurchaseResult
    data object Pending : PurchaseResult
    data object Cancelled : PurchaseResult
    data object BillingUnavailable : PurchaseResult
    /** Restore Purchases specifically: queried successfully, but nothing to restore. Not an error. */
    data object NothingToRestore : PurchaseResult
    data class Error(val message: String) : PurchaseResult
}

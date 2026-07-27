package com.runtimelabs.clarity.data.premium

import android.app.Activity
import android.content.Context
import android.util.Base64
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.runtimelabs.clarity.domain.premium.BillingConnector
import com.runtimelabs.clarity.domain.premium.PremiumState
import com.runtimelabs.clarity.domain.premium.PurchaseResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The real Google Play Billing v8 implementation of [BillingConnector].
 * One-time, non-consumable product only — [REMOVE_ADS_PRODUCT_ID], no
 * subscriptions. Every method here does exactly what its [BillingConnector]
 * doc comment promises; nothing here changes what [BillingConnector]
 * guarantees to its callers, per the seam's whole purpose (ARCHITECTURE.md
 * §24, §27).
 *
 * No cloud backend, by requirement — so purchases are verified client-side
 * against the app's Play Console license key (a genuine, honest trade-off:
 * this stops casual tampering, not a sufficiently motivated attacker with
 * root on their own device; a real backend doing server-side verification
 * is the stronger option if one is ever added). See [PLAY_CONSOLE_PUBLIC_KEY_BASE64].
 */
@Singleton
class PlayBillingConnector @Inject constructor(
    @ApplicationContext private val context: Context,
) : BillingConnector {

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        pendingPurchaseFlow?.complete(billingResult to purchases)
        pendingPurchaseFlow = null
    }

    /**
     * Set immediately before calling `launchBillingFlow` and completed by
     * [purchasesUpdatedListener] whenever Play responds — bridges the
     * listener callback into [purchasePremium]'s suspend return. A single
     * field is enough: only one purchase flow can be on-screen at a time
     * for a single product with no backend to queue anything against.
     */
    @Volatile
    private var pendingPurchaseFlow: CompletableDeferred<Pair<BillingResult, List<Purchase>?>>? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            // enableOneTimeProducts() is NOT implicit in v8 (the deprecated
            // no-arg enablePendingPurchases() used to assume it) — omitting
            // this silently breaks pending purchases for cash/bank-transfer
            // payment methods in markets like Japan, Brazil, and Indonesia.
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build(),
        )
        .enableAutoServiceReconnection()
        .build()

    private val connectionMutex = Mutex()

    /** Tracked by hand from the two confirmed callback methods below, rather than relying on an unverified BillingClient introspection API. */
    @Volatile
    private var isConnected = false

    /**
     * Ensures [billingClient] is connected before any call that needs it.
     * Guarded by a mutex so concurrent callers (e.g. startup verification
     * racing a user tapping "Restore Purchases") don't both call
     * `startConnection` at once. `enableAutoServiceReconnection()` handles
     * reconnecting after a drop; this only handles the very first connect.
     */
    private suspend fun ensureConnected(): BillingResponseCodeOutcome = connectionMutex.withLock {
        if (isConnected) return@withLock BillingResponseCodeOutcome.Connected
        val deferred = CompletableDeferred<Int>()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isConnected = billingResult.responseCode == BillingResponseCode.OK
                deferred.complete(billingResult.responseCode)
            }

            override fun onBillingServiceDisconnected() {
                // enableAutoServiceReconnection() handles retries; this just
                // keeps our own tracked state honest, and unblocks the
                // await below if setup never got the chance to finish.
                isConnected = false
                if (!deferred.isCompleted) deferred.complete(BillingResponseCode.SERVICE_DISCONNECTED)
            }
        })
        return@withLock if (deferred.await() == BillingResponseCode.OK) {
            BillingResponseCodeOutcome.Connected
        } else {
            BillingResponseCodeOutcome.Unavailable
        }
    }

    private enum class BillingResponseCodeOutcome { Connected, Unavailable }

    override suspend fun queryEntitlement(): PremiumState {
        if (ensureConnected() != BillingResponseCodeOutcome.Connected) return PremiumState.Free
        val purchase = findVerifiedRemoveAdsPurchase() ?: return PremiumState.Free
        return purchase.toPremiumState()
    }

    override suspend fun purchasePremium(activity: Activity): PurchaseResult {
        if (ensureConnected() != BillingResponseCodeOutcome.Connected) return PurchaseResult.BillingUnavailable

        val productDetails = queryRemoveAdsProductDetails()
            ?: return PurchaseResult.Error("Remove Ads is not available for purchase right now.")

        val offerToken = productDetails.oneTimePurchaseOfferDetails?.offerToken
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .apply { if (offerToken != null) setOfferToken(offerToken) }
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val deferred = CompletableDeferred<Pair<BillingResult, List<Purchase>?>>()
        pendingPurchaseFlow = deferred

        val launchResult = billingClient.launchBillingFlow(activity, flowParams)
        if (launchResult.responseCode != BillingResponseCode.OK) {
            pendingPurchaseFlow = null
            return launchResult.responseCode.toPurchaseResult()
        }

        val (billingResult, purchases) = deferred.await()
        if (billingResult.responseCode != BillingResponseCode.OK) {
            return billingResult.responseCode.toPurchaseResult()
        }

        val purchase = purchases?.firstOrNull { REMOVE_ADS_PRODUCT_ID in it.products }
            ?: return PurchaseResult.Error("No purchase was returned for Remove Ads.")

        return processPurchase(purchase).also { result ->
            if (result is PurchaseResult.Success || result is PurchaseResult.Pending) {
                Log.i(TAG, "Remove Ads purchase resolved: $result")
            }
        }
    }

    override suspend fun restorePurchases(): PurchaseResult {
        if (ensureConnected() != BillingResponseCodeOutcome.Connected) return PurchaseResult.BillingUnavailable
        val purchase = findVerifiedRemoveAdsPurchase() ?: return PurchaseResult.NothingToRestore
        return processPurchase(purchase)
    }

    /** Shared by [queryEntitlement] and [restorePurchases]: find, verify, but do not acknowledge (read-only). */
    private suspend fun findVerifiedRemoveAdsPurchase(): Purchase? {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val result = billingClient.queryPurchasesAsync(params)
        if (result.billingResult.responseCode != BillingResponseCode.OK) return null
        return result.purchasesList
            .firstOrNull { REMOVE_ADS_PRODUCT_ID in it.products }
            ?.takeIf { verifySignature(it) }
    }

    /** Verifies, and for a genuinely completed purchase, acknowledges it — required within 3 days or Play auto-refunds it. */
    private suspend fun processPurchase(purchase: Purchase): PurchaseResult {
        if (!verifySignature(purchase)) {
            return PurchaseResult.Error("Purchase could not be verified.")
        }
        return when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                if (!purchase.isAcknowledged) {
                    val ackResult = acknowledgePurchase(purchase)
                    if (ackResult.responseCode != BillingResponseCode.OK) {
                        return PurchaseResult.Error("Could not confirm the purchase with Google Play.")
                    }
                }
                PurchaseResult.Success
            }
            Purchase.PurchaseState.PENDING -> PurchaseResult.Pending
            else -> PurchaseResult.Error("Unrecognized purchase state.")
        }
    }

    /**
     * Wrapped by hand rather than assumed to have a clean suspend ktx form:
     * this library's naming is inconsistent (`queryProductDetails` drops
     * "Async", `queryPurchasesAsync` keeps it) — only the callback-based
     * overload is confirmed everywhere, so that's the one this relies on.
     */
    private suspend fun acknowledgePurchase(purchase: Purchase): BillingResult {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        return suspendCancellableCoroutine { continuation ->
            billingClient.acknowledgePurchase(params) { billingResult ->
                continuation.resume(billingResult)
            }
        }
    }

    private suspend fun queryRemoveAdsProductDetails(): ProductDetails? {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(REMOVE_ADS_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()
        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode != BillingResponseCode.OK) return null
        // Safe call: this Billing Library version's Kotlin-visible signature
        // for productDetailsList is List<ProductDetails>? (nullable), not
        // the non-null List some docs/samples show for other versions —
        // trusting the compiler's own reported type here over a generic
        // sample rather than assuming non-null.
        return result.productDetailsList?.firstOrNull()
    }

    private fun Purchase.toPremiumState(): PremiumState = when (purchaseState) {
        Purchase.PurchaseState.PURCHASED -> PremiumState.Premium
        Purchase.PurchaseState.PENDING -> PremiumState.Pending
        else -> PremiumState.Free
    }

    // Receiver is Int, not BillingResponseCode: BillingClient.BillingResponseCode is a
    // @Retention(SOURCE) @IntDef-style annotation used purely to mark Int
    // constants for lint, not an actual assignable type — every real
    // response code value throughout this API (BillingResult.responseCode
    // included) is plain Int. BillingResponseCode.USER_CANCELED etc. below
    // are still exactly right as-is: those reference the namespaced Int
    // constants, which is the only thing BillingResponseCode is actually
    // for.
    private fun Int.toPurchaseResult(): PurchaseResult = when (this) {
        BillingResponseCode.USER_CANCELED -> PurchaseResult.Cancelled
        BillingResponseCode.ITEM_ALREADY_OWNED -> PurchaseResult.Success
        BillingResponseCode.SERVICE_DISCONNECTED,
        BillingResponseCode.SERVICE_UNAVAILABLE,
        BillingResponseCode.BILLING_UNAVAILABLE,
        BillingResponseCode.FEATURE_NOT_SUPPORTED,
        -> PurchaseResult.BillingUnavailable
        else -> PurchaseResult.Error("Purchase failed (code $this).")
    }

    /**
     * Client-side signature verification — the honest trade-off for "no
     * cloud backend": this defends against casual tampering, not a
     * sufficiently motivated attacker with root on their own device (only
     * server-side verification of the purchase token against Play's own
     * servers can fully close that gap). Fails CLOSED: if the placeholder
     * key is still present, every purchase is rejected as unverified rather
     * than silently accepted, so forgetting to configure this cannot
     * accidentally grant free premium to everyone.
     */
    private fun verifySignature(purchase: Purchase): Boolean {
        if (PLAY_CONSOLE_PUBLIC_KEY_BASE64 == PLACEHOLDER_KEY) {
            Log.w(
                TAG,
                "PLAY_CONSOLE_PUBLIC_KEY_BASE64 is still the placeholder value — " +
                    "rejecting all purchases as unverified until this is configured. " +
                    "Find your real key in Play Console: Setup > App integrity > Licensing.",
            )
            return false
        }
        return try {
            val publicKey = decodePublicKey(PLAY_CONSOLE_PUBLIC_KEY_BASE64)
            val signature = Signature.getInstance("SHA1withRSA").apply {
                initVerify(publicKey)
                update(purchase.originalJson.toByteArray())
            }
            signature.verify(Base64.decode(purchase.signature, Base64.DEFAULT))
        } catch (e: GeneralSecurityException) {
            // Covers InvalidKeySpecException, SignatureException, InvalidKeyException,
            // and NoSuchAlgorithmException — everything KeyFactory/Signature can
            // genuinely throw, so a malformed key or corrupted purchase fails
            // gracefully (verification = false) instead of crashing the app.
            Log.e(TAG, "Purchase signature verification failed", e)
            false
        } catch (e: IllegalArgumentException) {
            // Base64.decode on malformed input.
            Log.e(TAG, "Purchase signature verification failed", e)
            false
        }
    }

    private fun decodePublicKey(base64Key: String): PublicKey {
        val keyBytes = Base64.decode(base64Key, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePublic(keySpec)
    }

    private companion object {
        const val TAG = "PlayBillingConnector"
        const val REMOVE_ADS_PRODUCT_ID = "remove_ads"

        const val PLACEHOLDER_KEY = "REPLACE_WITH_YOUR_PLAY_CONSOLE_BASE64_PUBLIC_KEY"

        // TODO: Replace with your app's actual Base64-encoded RSA public key
        // before your first release. Find it in Play Console under
        // Setup > App integrity > Licensing. This is a PUBLIC key — it is
        // meant to ship inside the app binary and is not a secret by itself,
        // but it is genuinely per-app and verification is meaningless
        // (and fails closed, per verifySignature's doc comment) until this
        // is the real value.
        const val PLAY_CONSOLE_PUBLIC_KEY_BASE64 = PLACEHOLDER_KEY
    }
}

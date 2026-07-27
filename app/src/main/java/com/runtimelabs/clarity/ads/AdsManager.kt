package com.runtimelabs.clarity.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.runtimelabs.clarity.BuildConfig
import com.runtimelabs.clarity.domain.ads.AdPolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The single place that talks to Google Mobile Ads and UMP. Every other
 * class touches ads only through this or through [AdPolicy] (pure, no SDK
 * dependency). See ARCHITECTURE.md §23 for why this exists at all and what
 * it costs the app's privacy posture.
 *
 * Sequencing matters and is enforced here, not left to callers: UMP consent
 * is requested and resolved BEFORE `MobileAds.initialize()` ever runs. This
 * follows Google's own documented order — asking before tracking, not the
 * other way around.
 */
@Singleton
class AdsManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val consentInformation: ConsentInformation by lazy {
        UserMessagingPlatform.getConsentInformation(context)
    }

    private var sdkInitialized = false

    private val _canRequestAds = MutableStateFlow(false)

    /**
     * Reactive on purpose: consent resolution is asynchronous (it may show
     * a form and wait on the person), so anything gating a banner on this
     * needs to recompose once it flips true, not just read it once at
     * first composition.
     */
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val _isPrivacyOptionsRequired = MutableStateFlow(false)

    /** Same reactivity reasoning as [canRequestAds] — this is only known after consent resolves. */
    val isPrivacyOptionsRequired: StateFlow<Boolean> = _isPrivacyOptionsRequired.asStateFlow()

    /** Resolved once per process: test creative in debug, your real unit in release. */
    val bannerAdUnitId: String = AdPolicy.selectBannerAdUnitId(
        isDebugBuild = BuildConfig.DEBUG,
        testAdUnitId = TEST_BANNER_AD_UNIT_ID,
        productionAdUnitId = PRODUCTION_BANNER_AD_UNIT_ID,
    )

    /** Call once, from MainActivity.onCreate(). Safe to call more than once (no-ops if already resolved this process). */
    fun requestConsentAndInitialize(activity: Activity) {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Consent info is current. Load a form only if one is
                // actually required — the callback fires immediately
                // otherwise (Google's own documented behavior).
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    initializeIfConsentAllows(activity)
                }
            },
            {
                // Update failed — fall back to whatever consent status
                // survived from a previous session, if any (Google's own
                // documented guidance for this failure path).
                initializeIfConsentAllows(activity)
            },
        )
    }

    /** Drives a privacy-options entry point wherever the app puts one (currently Home; see HomeScreen.kt). */
    fun showPrivacyOptionsForm(activity: Activity, onDismissed: (FormError?) -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onDismissed)
    }

    private fun initializeIfConsentAllows(context: Context) {
        _canRequestAds.value = consentInformation.canRequestAds()
        _isPrivacyOptionsRequired.value = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        if (!consentInformation.canRequestAds() || sdkInitialized) return
        sdkInitialized = true
        MobileAds.initialize(context)
    }

    private companion object {
        // Google's own demo IDs — not tied to any AdMob account, safe to
        // hardcode, always returns test creative. Never replace this with a
        // real unit id; that's what BuildConfig.DEBUG branching is for.
        const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val PRODUCTION_BANNER_AD_UNIT_ID = "ca-app-pub-9035742345664521/4108743307"
    }
}

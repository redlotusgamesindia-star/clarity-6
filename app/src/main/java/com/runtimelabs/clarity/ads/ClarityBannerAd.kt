package com.runtimelabs.clarity.ads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.runtimelabs.clarity.core.designsystem.theme.MotionTokens
import com.runtimelabs.clarity.domain.ads.AdPolicy
import com.runtimelabs.clarity.domain.ads.AdScreen

/**
 * A single banner, or nothing at all — never an empty placeholder box.
 * Renders zero layout space unless every one of these is true: [screen] is
 * in the allow-list, the person isn't premium, UMP has confirmed ads can be
 * requested, AND the ad actually finished loading. That last condition is
 * new: previously this always reserved space for the [AndroidView] the
 * moment policy allowed it, regardless of whether a real ad ever rendered
 * — a network failure would leave a blank gray box sitting in the layout.
 * Now nothing is shown until `onAdLoaded` actually fires, and a failure
 * collapses back to zero space rather than leaving one behind.
 *
 * Fixed-size banner (not adaptive) — a deliberate simplification carried
 * over from the first pass; adaptive sizing remains a reasonable later
 * upgrade, not a correctness issue today.
 */
@Composable
fun ClarityBannerAd(
    screen: AdScreen,
    modifier: Modifier = Modifier,
    viewModel: BannerAdViewModel = hiltViewModel(),
) {
    val isPremiumUser by viewModel.isPremiumUser.collectAsStateWithLifecycle()
    val canRequestAds by viewModel.canRequestAds.collectAsStateWithLifecycle()
    val policyAllows = remember(screen, isPremiumUser, canRequestAds) {
        AdPolicy.isAdsAllowed(screen, isPremiumUser) && canRequestAds
    }

    // Distinct from policyAllows: this tracks whether an actual ad loaded,
    // driving the animated reveal and the automatic hide-on-failure.
    var hasLoadedAd by remember { mutableStateOf(false) }

    if (!policyAllows) return

    val context = LocalContext.current
    // bannerAdUnitId never changes during a process's lifetime (resolved
    // once in AdsManager), so this creates the AdView exactly once per
    // composition slot — no key needed, unlike an actually-reactive value.
    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = viewModel.bannerAdUnitId
        }
    }

    // AdView is a classic Android View with its own lifecycle methods that
    // Compose knows nothing about. destroy()-on-dispose is the one that
    // matters for correctness (leaked native ad resources otherwise);
    // pause()/resume() tied to the Activity's own lifecycle would be a
    // reasonable follow-up for refresh/battery efficiency but is skipped
    // here deliberately rather than adding LifecycleEventObserver plumbing
    // for a banner that's off-screen entirely on every sensitive screen.
    DisposableEffect(adView) {
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                hasLoadedAd = true
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                hasLoadedAd = false
            }
        }
        adView.loadAd(AdRequest.Builder().build())
        onDispose { adView.destroy() }
    }

    AnimatedVisibility(
        visible = hasLoadedAd,
        enter = fadeIn(tween(MotionTokens.STANDARD)) +
            slideInVertically(tween(MotionTokens.STANDARD)) { it / 4 },
        exit = fadeOut(tween(MotionTokens.QUICK)),
    ) {
        AndroidView(
            factory = { adView },
            modifier = modifier.fillMaxWidth(),
        )
    }
}

package com.runtimelabs.clarity.ads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.premium.PremiumManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Thin bridge so [ClarityBannerAd] can reach [AdsManager] and the premium
 * flag the same way every other screen in this app reaches its
 * dependencies — through a Hilt ViewModel, not a raw injected singleton
 * grabbed some other way.
 */
@HiltViewModel
class BannerAdViewModel @Inject constructor(
    premiumManager: PremiumManager,
    private val adsManager: AdsManager,
) : ViewModel() {

    val isPremiumUser: StateFlow<Boolean> = premiumManager.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = false)

    val canRequestAds: StateFlow<Boolean> = adsManager.canRequestAds

    val bannerAdUnitId: String get() = adsManager.bannerAdUnitId
}

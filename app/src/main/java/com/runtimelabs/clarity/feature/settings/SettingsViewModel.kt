package com.runtimelabs.clarity.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.premium.PremiumState
import com.runtimelabs.clarity.premium.PremiumManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Reads [PremiumManager] only — never [com.runtimelabs.clarity.ads.AdsManager]
 * or any AdMob type directly, matching "no UI should directly reference
 * AdMob, everything must go through AdsManager" (and, transitively here,
 * through PremiumManager for anything premium-status-shaped).
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    premiumManager: PremiumManager,
) : ViewModel() {

    val premiumState: StateFlow<PremiumState> = premiumManager.premiumState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = PremiumState.Free)
}

package com.runtimelabs.clarity.feature.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.premium.PremiumState
import com.runtimelabs.clarity.domain.premium.PurchaseResult
import com.runtimelabs.clarity.premium.PremiumManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val premiumState: PremiumState = PremiumState.Free,
    val isPurchasing: Boolean = false,
    val isRestoring: Boolean = false,
    /** One-shot: the outcome of the most recent purchase/restore attempt, consumed then cleared. */
    val purchaseResult: PurchaseResult? = null,
) {
    val isBusy: Boolean get() = isPurchasing || isRestoring
}

/**
 * Reads and calls [PremiumManager] only — never [com.runtimelabs.clarity.ads.AdsManager],
 * [com.runtimelabs.clarity.domain.premium.BillingConnector], or any Play
 * Billing type directly, matching "no UI should directly reference AdMob,
 * everything must go through AdsManager" extended to its premium
 * equivalent: everything here goes through PremiumManager.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val premiumManager: PremiumManager,
) : ViewModel() {

    private val isPurchasing = MutableStateFlow(false)
    private val isRestoring = MutableStateFlow(false)
    private val purchaseResult = MutableStateFlow<PurchaseResult?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        premiumManager.premiumState,
        isPurchasing,
        isRestoring,
        purchaseResult,
    ) { premium, purchasing, restoring, result ->
        SettingsUiState(
            premiumState = premium,
            isPurchasing = purchasing,
            isRestoring = restoring,
            purchaseResult = result,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun onRemoveAdsClicked(activity: Activity) {
        if (isPurchasing.value || isRestoring.value) return
        viewModelScope.launch {
            isPurchasing.value = true
            purchaseResult.value = premiumManager.purchasePremium(activity)
            isPurchasing.value = false
        }
    }

    fun onRestorePurchasesClicked() {
        if (isPurchasing.value || isRestoring.value) return
        viewModelScope.launch {
            isRestoring.value = true
            purchaseResult.value = premiumManager.restorePurchases()
            isRestoring.value = false
        }
    }

    /** Called once the screen has shown feedback for [SettingsUiState.purchaseResult], so it doesn't reappear on recomposition. */
    fun onPurchaseResultConsumed() {
        purchaseResult.value = null
    }
}

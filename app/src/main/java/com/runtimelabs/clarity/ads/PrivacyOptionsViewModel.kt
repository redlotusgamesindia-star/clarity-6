package com.runtimelabs.clarity.ads

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.google.android.ump.FormError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/**
 * Bridges [AdsManager]'s privacy-options state into Compose. Separate from
 * [BannerAdViewModel] on purpose — this is a different concern (revisiting
 * a consent choice, not gating a banner) even though both are thin
 * passthroughs to the same manager.
 */
@HiltViewModel
class PrivacyOptionsViewModel @Inject constructor(
    private val adsManager: AdsManager,
) : ViewModel() {

    val isPrivacyOptionsRequired: StateFlow<Boolean> = adsManager.isPrivacyOptionsRequired

    fun showPrivacyOptionsForm(activity: Activity, onDismissed: (FormError?) -> Unit) =
        adsManager.showPrivacyOptionsForm(activity, onDismissed)
}

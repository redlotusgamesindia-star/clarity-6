package com.runtimelabs.clarity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtimelabs.clarity.ads.AdsManager
import com.runtimelabs.clarity.core.designsystem.theme.ClarityTheme
import com.runtimelabs.clarity.core.designsystem.theme.shouldUseDarkTheme
import com.runtimelabs.clarity.ui.ClarityAppRoot
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-activity architecture: this is the only Activity in the app.
 * All "screens" are Navigation Compose destinations.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var adsManager: AdsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Hold the system splash until root state (theme + start destination)
        // is loaded. DataStore reads are single-digit ms, so this is invisible
        // in practice but eliminates theme-flash on cold start entirely.
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value is MainUiState.Loading
        }

        enableEdgeToEdge()

        // UMP consent, then (only if allowed) the ads SDK — every launch,
        // per UMP's own documented guidance. Needs an Activity, so this
        // can't live inside AdsManager's own construction.
        adsManager.requestConsentAndInitialize(this)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            when (val state = uiState) {
                is MainUiState.Loading -> Unit // splash still covering the window
                is MainUiState.Ready -> {
                    ClarityTheme(darkTheme = shouldUseDarkTheme(state.themeMode)) {
                        ClarityAppRoot(onboardingCompleted = state.onboardingCompleted)
                    }
                }
            }
        }
    }
}

package com.runtimelabs.clarity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.model.ThemeMode
import com.runtimelabs.clarity.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Root-level state: everything the app shell must know before it can draw
 * its first meaningful frame (theme + whether onboarding is complete).
 *
 * The splash screen is held on screen while this is [MainUiState.Loading],
 * so there is never a flash of the wrong theme or the wrong start screen.
 */
sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(
        val themeMode: ThemeMode,
        val onboardingCompleted: Boolean,
    ) : MainUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    // Eagerly, not WhileSubscribed: the splash keep-on-screen condition reads
    // `uiState.value` before the first composition subscribes, so the upstream
    // must already be collecting or the splash would never dismiss.
    val uiState: StateFlow<MainUiState> =
        combine(
            settingsRepository.themeMode,
            settingsRepository.onboardingCompleted,
        ) { themeMode, onboardingCompleted ->
            MainUiState.Ready(themeMode, onboardingCompleted) as MainUiState
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MainUiState.Loading,
        )
}

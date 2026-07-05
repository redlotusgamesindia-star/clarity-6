package com.runtimelabs.clarity.feature.toolkit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.model.ReasonToQuit
import com.runtimelabs.clarity.domain.model.RecoveryGoal
import com.runtimelabs.clarity.domain.repository.RecoveryProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface WhyUiState {
    data object Loading : WhyUiState
    data object Empty : WhyUiState
    data class Ready(
        val reasons: List<ReasonToQuit>,
        val goal: RecoveryGoal,
    ) : WhyUiState
}

/** Reads the onboarding answers back — the user's own words as the tool. */
@HiltViewModel
class WhyViewModel @Inject constructor(
    profileRepository: RecoveryProfileRepository,
) : ViewModel() {

    val uiState: StateFlow<WhyUiState> = profileRepository.profile
        .map { profile ->
            if (profile == null) {
                WhyUiState.Empty
            } else {
                WhyUiState.Ready(reasons = profile.reasonsToQuit, goal = profile.goal)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WhyUiState.Loading,
        )
}

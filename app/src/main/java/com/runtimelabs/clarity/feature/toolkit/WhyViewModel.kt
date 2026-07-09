package com.runtimelabs.clarity.feature.toolkit

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.model.ReasonToQuit
import com.runtimelabs.clarity.domain.model.RecoveryGoal
import com.runtimelabs.clarity.domain.repository.RecoveryProfileRepository
import com.runtimelabs.clarity.domain.repository.ToolkitUsageRepository
import com.runtimelabs.clarity.domain.toolkit.ToolkitTool
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface WhyUiState {
    data object Loading : WhyUiState
    data object Empty : WhyUiState

    /** @Immutable: the `List<ReasonToQuit>` field is otherwise conservatively-unstable — see JournalUiState's fuller rationale. */
    @Immutable
    data class Ready(
        val reasons: List<ReasonToQuit>,
        val goal: RecoveryGoal,
    ) : WhyUiState
}

/** Reads the onboarding answers back — the user's own words as the tool. Also the Motivation Wall entry in the Emergency Toolkit. */
@HiltViewModel
class WhyViewModel @Inject constructor(
    profileRepository: RecoveryProfileRepository,
    private val toolkitUsageRepository: ToolkitUsageRepository,
) : ViewModel() {

    private val startedAtMillis = System.currentTimeMillis()

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

    fun onExit() {
        viewModelScope.launch {
            val elapsedSeconds = ((System.currentTimeMillis() - startedAtMillis) / 1_000).toInt()
            toolkitUsageRepository.record(ToolkitTool.MOTIVATION_WALL, elapsedSeconds.coerceAtLeast(0))
        }
    }
}

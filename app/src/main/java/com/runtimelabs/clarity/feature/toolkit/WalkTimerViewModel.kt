package com.runtimelabs.clarity.feature.toolkit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.repository.ToolkitUsageRepository
import com.runtimelabs.clarity.domain.toolkit.ToolkitTool
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class WalkTimerUiState(
    val elapsedSeconds: Int = 0,
    val finished: Boolean = false,
)

/** A genuine live count-up timer — the one tool here whose whole point is watching time pass. */
@HiltViewModel
class WalkTimerViewModel @Inject constructor(
    private val toolkitUsageRepository: ToolkitUsageRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalkTimerUiState())
    val uiState = _uiState.asStateFlow()

    private var tickJob: Job? = null

    init {
        tickJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    fun onMadeItThrough() {
        if (_uiState.value.finished) return
        tickJob?.cancel()
        val elapsed = _uiState.value.elapsedSeconds
        viewModelScope.launch {
            toolkitUsageRepository.record(ToolkitTool.WALK_OUTSIDE, elapsed)
            _uiState.update { it.copy(finished = true) }
        }
    }
}

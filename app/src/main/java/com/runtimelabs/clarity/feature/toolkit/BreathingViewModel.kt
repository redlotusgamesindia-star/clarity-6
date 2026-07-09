package com.runtimelabs.clarity.feature.toolkit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.model.JourneyEvent
import com.runtimelabs.clarity.domain.model.JourneyEventType
import com.runtimelabs.clarity.domain.repository.JourneyRepository
import com.runtimelabs.clarity.domain.repository.ToolkitUsageRepository
import com.runtimelabs.clarity.domain.toolkit.ToolkitTool
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class BreathingUiState(
    val session: BreathingSessionState = BreathingSessionState(BreathingPatterns.CALM),
    val finished: Boolean = false,
)

@HiltViewModel
class BreathingViewModel @Inject constructor(
    private val journeyRepository: JourneyRepository,
    private val toolkitUsageRepository: ToolkitUsageRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BreathingUiState())
    val uiState = _uiState.asStateFlow()

    private var tickJob: Job? = null
    private var hasSetTarget = false

    init {
        // Auto-start: nobody in a spike wants a settings screen first.
        startTicking()
    }

    /**
     * Called once from [BreathingScreen] via the route's target duration —
     * same "composable receives the route arg, hands it to the VM" pattern
     * as [com.runtimelabs.clarity.feature.recovery.RecoveryFlowScreen], not
     * a SavedStateHandle. A no-op after the first call so a recomposition
     * can't reset an in-progress session back to zero.
     */
    fun setTargetDuration(seconds: Int) {
        if (hasSetTarget) return
        hasSetTarget = true
        if (seconds <= 0) return // 0 is the open-ended default; nothing to change
        _uiState.update { it.copy(session = it.session.copy(targetDurationSeconds = seconds)) }
    }

    private fun startTicking() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                _uiState.update { it.copy(session = advanceOneSecond(it.session)) }
            }
        }
    }

    fun onPatternSelected(code: String) {
        _uiState.update {
            it.copy(
                session = BreathingSessionState(
                    pattern = BreathingPatterns.byCode(code),
                    targetDurationSeconds = it.session.targetDurationSeconds,
                ),
            )
        }
    }

    /**
     * Ends the session — reaching a set target, the button, or system back
     * all funnel through here. Sessions that lasted at least
     * [MIN_LOGGED_SECONDS] become a BREATHING_SESSION journey event
     * (unchanged, append-only fact the insight engine can use later) AND a
     * [ToolkitUsageRepository] record (new: feeds "times used" / "most
     * used tool" / "average urge duration" on Journey). Both are cheap to
     * keep recording even though only the second is read anywhere yet.
     */
    fun onEndSession() {
        if (_uiState.value.finished) return
        tickJob?.cancel()
        val elapsed = _uiState.value.session.elapsedSeconds
        val target = _uiState.value.session.targetDurationSeconds
        viewModelScope.launch {
            if (elapsed >= MIN_LOGGED_SECONDS) {
                journeyRepository.record(
                    JourneyEvent(
                        id = 0,
                        type = JourneyEventType.BREATHING_SESSION,
                        occurredAtEpochMillis = System.currentTimeMillis(),
                        epochDay = LocalDate.now().toEpochDay(),
                    ),
                )
                toolkitUsageRepository.record(tool = toolFor(target), durationSeconds = elapsed)
            }
            _uiState.update { it.copy(finished = true) }
        }
    }

    private fun toolFor(targetDurationSeconds: Int): ToolkitTool = when (targetDurationSeconds) {
        30 -> ToolkitTool.BREATHING_30S
        60 -> ToolkitTool.BREATHING_60S
        120 -> ToolkitTool.BREATHING_2MIN
        else -> ToolkitTool.BREATHING_OPEN
    }

    private companion object {
        const val MIN_LOGGED_SECONDS = 30
    }
}

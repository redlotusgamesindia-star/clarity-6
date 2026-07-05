package com.runtimelabs.clarity.feature.toolkit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.model.JourneyEvent
import com.runtimelabs.clarity.domain.model.JourneyEventType
import com.runtimelabs.clarity.domain.repository.JourneyRepository
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(BreathingUiState())
    val uiState = _uiState.asStateFlow()

    private var tickJob: Job? = null

    init {
        // Auto-start: nobody in a spike wants a settings screen first.
        startTicking()
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
        _uiState.update { it.copy(session = BreathingSessionState(BreathingPatterns.byCode(code))) }
    }

    /**
     * Ends the session (button or system back). Sessions that lasted at
     * least [MIN_LOGGED_SECONDS] become a BREATHING_SESSION journey event —
     * append-only facts the insight engine can use later.
     */
    fun onEndSession() {
        if (_uiState.value.finished) return
        tickJob?.cancel()
        val elapsed = _uiState.value.session.elapsedSeconds
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
            }
            _uiState.update { it.copy(finished = true) }
        }
    }

    private companion object {
        const val MIN_LOGGED_SECONDS = 30
    }
}

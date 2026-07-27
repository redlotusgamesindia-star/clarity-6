package com.runtimelabs.clarity.feature.journal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.runtimelabs.clarity.domain.model.ThoughtRecord
import com.runtimelabs.clarity.domain.repository.ThoughtRecordRepository
import com.runtimelabs.clarity.navigation.ThoughtRecordEditorRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ThoughtRecordUiState(
    val isLoading: Boolean,
    val recordId: Long,
    val epochDay: Long,
    val createdAtEpochMillis: Long,
    val situation: String = "",
    val automaticThought: String = "",
    val feeling: String = "",
    val feelingIntensity: Int = 5,
    val reframe: String = "",
    val isSaving: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val finished: Boolean = false,
) {
    val isExisting: Boolean get() = recordId != ThoughtRecord.NEW_ID

    // Reframe is optional at save time on purpose: naming the thought and
    // feeling is already the exercise; the reframe can come later or never.
    val canSave: Boolean get() = situation.isNotBlank() && automaticThought.isNotBlank()
}

@HiltViewModel
class ThoughtRecordViewModel @Inject constructor(
    private val repository: ThoughtRecordRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ThoughtRecordEditorRoute>()

    private val _uiState = MutableStateFlow(
        ThoughtRecordUiState(
            isLoading = route.recordId != ThoughtRecord.NEW_ID,
            recordId = route.recordId,
            epochDay = LocalDate.now().toEpochDay(),
            createdAtEpochMillis = System.currentTimeMillis(),
        ),
    )
    val uiState = _uiState.asStateFlow()

    init {
        if (route.recordId != ThoughtRecord.NEW_ID) {
            viewModelScope.launch {
                val record = repository.getById(route.recordId)
                _uiState.update { state ->
                    if (record == null) {
                        state.copy(isLoading = false, finished = true)
                    } else {
                        state.copy(
                            isLoading = false,
                            epochDay = record.epochDay,
                            createdAtEpochMillis = record.createdAtEpochMillis,
                            situation = record.situation,
                            automaticThought = record.automaticThought,
                            feeling = record.feeling,
                            feelingIntensity = record.feelingIntensity,
                            reframe = record.reframe,
                        )
                    }
                }
            }
        }
    }

    fun onSituationChanged(value: String) = _uiState.update { it.copy(situation = value) }
    fun onThoughtChanged(value: String) = _uiState.update { it.copy(automaticThought = value) }
    fun onFeelingChanged(value: String) = _uiState.update { it.copy(feeling = value) }
    fun onIntensityChanged(value: Int) = _uiState.update { it.copy(feelingIntensity = value.coerceIn(0, 10)) }
    fun onReframeChanged(value: String) = _uiState.update { it.copy(reframe = value) }

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave || state.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.save(
                ThoughtRecord(
                    id = state.recordId,
                    epochDay = state.epochDay,
                    createdAtEpochMillis = state.createdAtEpochMillis,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                    situation = state.situation.trim(),
                    automaticThought = state.automaticThought.trim(),
                    feeling = state.feeling.trim(),
                    feelingIntensity = state.feelingIntensity,
                    reframe = state.reframe.trim(),
                ),
            )
            _uiState.update { it.copy(finished = true) }
        }
    }

    fun onDeleteRequested() = _uiState.update { it.copy(showDeleteDialog = true) }
    fun onDeleteDismissed() = _uiState.update { it.copy(showDeleteDialog = false) }

    fun onDeleteConfirmed() {
        val state = _uiState.value
        if (!state.isExisting) return
        _uiState.update { it.copy(showDeleteDialog = false, isSaving = true) }
        viewModelScope.launch {
            repository.delete(state.recordId)
            _uiState.update { it.copy(finished = true) }
        }
    }
}

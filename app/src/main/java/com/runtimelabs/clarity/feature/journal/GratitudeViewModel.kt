package com.runtimelabs.clarity.feature.journal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.runtimelabs.clarity.domain.model.GratitudeEntry
import com.runtimelabs.clarity.domain.repository.GratitudeRepository
import com.runtimelabs.clarity.navigation.GratitudeEditorRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GratitudeUiState(
    val isLoading: Boolean,
    val entryId: Long,
    val epochDay: Long,
    val createdAtEpochMillis: Long,
    val first: String = "",
    val second: String = "",
    val third: String = "",
    val isSaving: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val finished: Boolean = false,
) {
    val isExisting: Boolean get() = entryId != GratitudeEntry.NEW_ID

    /** Only the first is required — pushing for three would turn this into a test. */
    val canSave: Boolean get() = first.isNotBlank()
}

@HiltViewModel
class GratitudeViewModel @Inject constructor(
    private val repository: GratitudeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<GratitudeEditorRoute>()

    private val _uiState = MutableStateFlow(
        GratitudeUiState(
            isLoading = route.entryId != GratitudeEntry.NEW_ID,
            entryId = route.entryId,
            epochDay = LocalDate.now().toEpochDay(),
            createdAtEpochMillis = System.currentTimeMillis(),
        ),
    )
    val uiState = _uiState.asStateFlow()

    init {
        if (route.entryId != GratitudeEntry.NEW_ID) {
            viewModelScope.launch {
                val entry = repository.getById(route.entryId)
                _uiState.update { state ->
                    if (entry == null) {
                        state.copy(isLoading = false, finished = true)
                    } else {
                        state.copy(
                            isLoading = false,
                            epochDay = entry.epochDay,
                            createdAtEpochMillis = entry.createdAtEpochMillis,
                            first = entry.first,
                            second = entry.second.orEmpty(),
                            third = entry.third.orEmpty(),
                        )
                    }
                }
            }
        }
    }

    fun onFirstChanged(value: String) = _uiState.update { it.copy(first = value) }
    fun onSecondChanged(value: String) = _uiState.update { it.copy(second = value) }
    fun onThirdChanged(value: String) = _uiState.update { it.copy(third = value) }

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave || state.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.save(
                GratitudeEntry(
                    id = state.entryId,
                    epochDay = state.epochDay,
                    createdAtEpochMillis = state.createdAtEpochMillis,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                    first = state.first.trim(),
                    second = state.second.trim().ifBlank { null },
                    third = state.third.trim().ifBlank { null },
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
            repository.delete(state.entryId)
            _uiState.update { it.copy(finished = true) }
        }
    }
}

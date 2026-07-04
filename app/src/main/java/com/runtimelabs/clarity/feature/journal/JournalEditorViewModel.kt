package com.runtimelabs.clarity.feature.journal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.runtimelabs.clarity.domain.model.JournalEntry
import com.runtimelabs.clarity.domain.repository.JournalRepository
import com.runtimelabs.clarity.navigation.JournalEditorRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JournalEditorUiState(
    val isLoading: Boolean,
    val entryId: Long,
    val epochDay: Long,
    val createdAtEpochMillis: Long,
    val body: String = "",
    val isSaving: Boolean = false,
    val showDeleteDialog: Boolean = false,
    /** Signals the screen to navigate back (saved, deleted, or entry missing). */
    val finished: Boolean = false,
) {
    val isExisting: Boolean get() = entryId != JournalEntry.NEW_ID
    val canSave: Boolean get() = body.isNotBlank()
}

@HiltViewModel
class JournalEditorViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<JournalEditorRoute>()

    private val _uiState = MutableStateFlow(
        JournalEditorUiState(
            isLoading = route.entryId != JournalEntry.NEW_ID,
            entryId = route.entryId,
            epochDay = LocalDate.now().toEpochDay(),
            createdAtEpochMillis = System.currentTimeMillis(),
        ),
    )
    val uiState = _uiState.asStateFlow()

    init {
        if (route.entryId != JournalEntry.NEW_ID) {
            viewModelScope.launch {
                val entry = journalRepository.getById(route.entryId)
                _uiState.update { state ->
                    if (entry == null) {
                        // Deleted elsewhere / bad deep link: nothing to edit.
                        state.copy(isLoading = false, finished = true)
                    } else {
                        state.copy(
                            isLoading = false,
                            epochDay = entry.epochDay,
                            createdAtEpochMillis = entry.createdAtEpochMillis,
                            body = entry.body,
                        )
                    }
                }
            }
        }
    }

    fun onBodyChanged(value: String) {
        _uiState.update { it.copy(body = value) }
    }

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave || state.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            journalRepository.save(
                JournalEntry(
                    id = state.entryId,
                    epochDay = state.epochDay,
                    createdAtEpochMillis = state.createdAtEpochMillis,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                    body = state.body.trim(),
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
            journalRepository.delete(state.entryId)
            _uiState.update { it.copy(finished = true) }
        }
    }
}

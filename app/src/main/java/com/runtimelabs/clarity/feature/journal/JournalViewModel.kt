package com.runtimelabs.clarity.feature.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.model.JournalEntry
import com.runtimelabs.clarity.domain.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface JournalUiState {
    data object Loading : JournalUiState
    data class Ready(val entries: List<JournalEntry>) : JournalUiState
}

@HiltViewModel
class JournalViewModel @Inject constructor(
    journalRepository: JournalRepository,
) : ViewModel() {

    val uiState: StateFlow<JournalUiState> = journalRepository.observeAll()
        .map { entries -> JournalUiState.Ready(entries) as JournalUiState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = JournalUiState.Loading,
        )
}

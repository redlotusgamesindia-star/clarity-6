package com.runtimelabs.clarity.feature.journal

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.repository.GratitudeRepository
import com.runtimelabs.clarity.domain.repository.JournalRepository
import com.runtimelabs.clarity.domain.repository.ThoughtRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface JournalUiState {
    data object Loading : JournalUiState

    /**
     * @Immutable — not just a `data class` convention here: this pass
     * marks every UiState type across the app the same way, since the
     * Compose compiler's stability inference treats a plain `List<T>`
     * field as unstable by default (it can't prove nothing else holds a
     * mutable reference to it), which silently disables recomposition
     * skipping for every screen reading this state. The list is in
     * practice always replaced wholesale, never mutated in place — the
     * same guarantee `@Immutable` exists to let a developer assert
     * directly when the compiler can't infer it on its own.
     */
    @Immutable
    data class Ready(val entries: List<JournalHubEntry>) : JournalUiState
}

/** All three entry kinds merged and sorted newest-first — one timeline. */
@HiltViewModel
class JournalViewModel @Inject constructor(
    journalRepository: JournalRepository,
    thoughtRecordRepository: ThoughtRecordRepository,
    gratitudeRepository: GratitudeRepository,
) : ViewModel() {

    val uiState: StateFlow<JournalUiState> = combine(
        journalRepository.observeAll(),
        thoughtRecordRepository.observeAll(),
        gratitudeRepository.observeAll(),
    ) { free, thoughts, gratitude ->
        val merged = (
            free.map { JournalHubEntry.from(it) } +
                thoughts.map { JournalHubEntry.from(it) } +
                gratitude.map { JournalHubEntry.from(it) }
            ).sortedByDescending { it.createdAtEpochMillis }
        JournalUiState.Ready(merged) as JournalUiState
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = JournalUiState.Loading,
    )
}

package com.runtimelabs.clarity.feature.journal

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.runtimelabs.clarity.domain.badge.Badge
import com.runtimelabs.clarity.domain.model.JournalEntry
import com.runtimelabs.clarity.domain.repository.BadgeRepository
import com.runtimelabs.clarity.domain.repository.JournalRepository
import com.runtimelabs.clarity.navigation.JournalEditorRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** @Immutable: the `List<Badge>` field is otherwise conservatively-unstable to the compiler — see JournalUiState's fuller rationale. */
@Immutable
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
    /** Badges genuinely earned by this save — e.g. Journal Writer. Shown before [finished] fires. */
    val newlyUnlockedBadges: List<Badge> = emptyList(),
) {
    val isExisting: Boolean get() = entryId != JournalEntry.NEW_ID
    val canSave: Boolean get() = body.isNotBlank()
}

@HiltViewModel
class JournalEditorViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    private val badgeRepository: BadgeRepository,
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
            // Journal Writer (and, incidentally, any streak badge a new day
            // crossed) can genuinely unlock right here. If it did, the
            // screen shows the celebration first — onCelebrationDismissed
            // is what actually sets `finished`, not this call directly.
            val newlyUnlocked = badgeRepository.evaluateAndUnlock()
            if (newlyUnlocked.isEmpty()) {
                _uiState.update { it.copy(finished = true) }
            } else {
                _uiState.update { it.copy(isSaving = false, newlyUnlockedBadges = newlyUnlocked) }
            }
        }
    }

    /** Called once the unlock celebration (if any) has been shown and dismissed. */
    fun onAchievementCelebrationDismissed() {
        _uiState.update { it.copy(newlyUnlockedBadges = emptyList(), finished = true) }
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

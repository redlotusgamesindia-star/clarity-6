package com.runtimelabs.clarity.feature.journey

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.runtimelabs.clarity.domain.model.Habit
import com.runtimelabs.clarity.domain.repository.HabitRepository
import com.runtimelabs.clarity.navigation.HabitEditorRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HabitEditorUiState(
    val isLoading: Boolean,
    val habitId: Long,
    val name: String = "",
    val iconCode: String = "spa",
    val daysMask: Int = Habit.ALL_DAYS_MASK,
    val reminderEnabled: Boolean = false,
    val reminderMinutes: Int = DEFAULT_REMINDER_MINUTES,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val showTimePicker: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isSaving: Boolean = false,
    val finished: Boolean = false,
) {
    val isExisting: Boolean get() = habitId != Habit.NEW_ID
    val canSave: Boolean get() = name.isNotBlank() && daysMask != 0

    companion object {
        const val DEFAULT_REMINDER_MINUTES = 20 * 60 // 8:00 PM
    }
}

@HiltViewModel
class HabitEditorViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<HabitEditorRoute>()

    private val _uiState = MutableStateFlow(
        HabitEditorUiState(
            isLoading = route.habitId != Habit.NEW_ID,
            habitId = route.habitId,
        ),
    )
    val uiState = _uiState.asStateFlow()

    init {
        if (route.habitId != Habit.NEW_ID) {
            viewModelScope.launch {
                val habit = habitRepository.getHabit(route.habitId)
                _uiState.update { state ->
                    if (habit == null) {
                        state.copy(isLoading = false, finished = true)
                    } else {
                        state.copy(
                            isLoading = false,
                            name = habit.name,
                            iconCode = habit.iconCode,
                            daysMask = habit.daysMask,
                            reminderEnabled = habit.reminderMinutesOfDay != null,
                            reminderMinutes = habit.reminderMinutesOfDay
                                ?: HabitEditorUiState.DEFAULT_REMINDER_MINUTES,
                            createdAtEpochMillis = habit.createdAtEpochMillis,
                        )
                    }
                }
            }
        }
    }

    fun onNameChanged(value: String) = _uiState.update { it.copy(name = value) }

    fun onIconSelected(code: String) = _uiState.update { it.copy(iconCode = code) }

    // A zero mask is allowed in the draft (canSave gates it) so toggling the
    // last day off doesn't mysteriously refuse — the disabled Save explains.
    fun onDayToggled(day: DayOfWeek) = _uiState.update {
        it.copy(daysMask = it.daysMask xor Habit.maskBit(day))
    }

    fun onReminderToggled(enabled: Boolean) = _uiState.update { it.copy(reminderEnabled = enabled) }

    fun onTimeClicked() = _uiState.update { it.copy(showTimePicker = true) }

    fun onTimePickerDismissed() = _uiState.update { it.copy(showTimePicker = false) }

    fun onTimePicked(hour: Int, minute: Int) = _uiState.update {
        it.copy(reminderMinutes = hour * 60 + minute, showTimePicker = false)
    }

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave || state.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            habitRepository.saveHabit(
                Habit(
                    id = state.habitId,
                    name = state.name.trim(),
                    iconCode = state.iconCode,
                    daysMask = state.daysMask,
                    reminderMinutesOfDay = if (state.reminderEnabled) state.reminderMinutes else null,
                    createdAtEpochMillis = state.createdAtEpochMillis,
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
            habitRepository.deleteHabit(state.habitId)
            _uiState.update { it.copy(finished = true) }
        }
    }
}

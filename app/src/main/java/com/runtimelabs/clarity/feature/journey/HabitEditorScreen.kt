package com.runtimelabs.clarity.feature.journey

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.BreathingIndicator
import com.runtimelabs.clarity.core.designsystem.components.ClarityCard
import com.runtimelabs.clarity.core.designsystem.components.ClarityPrimaryButton
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.domain.model.Habit
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/**
 * Create/edit a habit: name, icon, weekday schedule, optional reminder.
 * Enabling the reminder requests POST_NOTIFICATIONS on 13+; a denial still
 * saves the schedule (the alarm fires; the notification is dropped by the
 * system) and an inline hint says so honestly.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitEditorScreen(
    onDone: () -> Unit,
    viewModel: HabitEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var notificationsGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> notificationsGranted = granted }

    LaunchedEffect(state.finished) {
        if (state.finished) onDone()
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            EditorTopBar(state = state, onBack = onDone, onDelete = viewModel::onDeleteRequested)

            if (state.isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    BreathingIndicator(size = 48.dp)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
                ) {
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChanged,
                        label = { Text(stringResource(R.string.habit_name_label)) },
                        placeholder = { Text(stringResource(R.string.habit_name_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(MaterialTheme.spacing.lg))
                    Text(
                        text = stringResource(R.string.habit_icon_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    ) {
                        HABIT_ICON_CODES.forEach { code ->
                            SelectableCircle(
                                selected = state.iconCode == code,
                                onClick = { viewModel.onIconSelected(code) },
                                size = 48,
                            ) { tint ->
                                Icon(
                                    imageVector = habitIcon(code),
                                    contentDescription = stringResource(habitIconLabelRes(code)),
                                    tint = tint,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(MaterialTheme.spacing.lg))
                    Text(
                        text = stringResource(R.string.habit_days_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        DayOfWeek.entries.forEach { day ->
                            val selected = state.daysMask and Habit.maskBit(day) != 0
                            val label = remember(day) {
                                day.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                            }
                            SelectableCircle(
                                selected = selected,
                                onClick = { viewModel.onDayToggled(day) },
                                size = 40,
                            ) { tint ->
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = tint,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(MaterialTheme.spacing.lg))
                    ClarityCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.habit_reminder_label),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Spacer(Modifier.height(2.dp))
                                val timeLabel = if (state.reminderEnabled) {
                                    formatMinutesOfDay(state.reminderMinutes)
                                } else {
                                    stringResource(R.string.habit_reminder_off)
                                }
                                TextButton(
                                    onClick = viewModel::onTimeClicked,
                                    enabled = state.reminderEnabled,
                                ) {
                                    Text(timeLabel)
                                }
                            }
                            Switch(
                                checked = state.reminderEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && !notificationsGranted &&
                                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                                    ) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    viewModel.onReminderToggled(enabled)
                                },
                            )
                        }
                        if (state.reminderEnabled && !notificationsGranted) {
                            Spacer(Modifier.height(MaterialTheme.spacing.xs))
                            Text(
                                text = stringResource(R.string.habit_notifications_hint),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(MaterialTheme.spacing.xl))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.screenHorizontal)
                    .padding(top = MaterialTheme.spacing.sm, bottom = MaterialTheme.spacing.lg),
            ) {
                ClarityPrimaryButton(
                    text = stringResource(R.string.habit_save),
                    onClick = viewModel::onSave,
                    enabled = state.canSave && !state.isLoading,
                    loading = state.isSaving,
                )
            }
        }
    }

    if (state.showTimePicker) {
        TimePickerDialog(
            initialMinutes = state.reminderMinutes,
            onDismiss = viewModel::onTimePickerDismissed,
            onConfirm = viewModel::onTimePicked,
        )
    }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteDismissed,
            title = { Text(stringResource(R.string.habit_delete_title)) },
            text = { Text(stringResource(R.string.habit_delete_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onDeleteConfirmed) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDeleteDismissed) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

/** Shared circular selectable used by both the icon picker and day toggles. */
@Composable
private fun SelectableCircle(
    selected: Boolean,
    onClick: () -> Unit,
    size: Int,
    content: @Composable (tint: androidx.compose.ui.graphics.Color) -> Unit,
) {
    val tint = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            },
        ),
        modifier = Modifier.size(size.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            content(tint)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    val context = LocalContext.current
    val timeState = rememberTimePickerState(
        initialHour = initialMinutes / 60,
        initialMinute = initialMinutes % 60,
        is24Hour = android.text.format.DateFormat.is24HourFormat(context),
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.padding(MaterialTheme.spacing.lg)) {
                TimePicker(state = timeState)
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    TextButton(onClick = { onConfirm(timeState.hour, timeState.minute) }) {
                        Text(stringResource(R.string.action_ok))
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorTopBar(
    state: HabitEditorUiState,
    onBack: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = MaterialTheme.spacing.sm),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
            )
        }
        Text(
            text = stringResource(
                if (state.isExisting) R.string.habit_editor_edit_title else R.string.habit_editor_new_title,
            ),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = MaterialTheme.spacing.xs),
        )
        if (state.isExisting) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = stringResource(R.string.cd_delete_habit),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

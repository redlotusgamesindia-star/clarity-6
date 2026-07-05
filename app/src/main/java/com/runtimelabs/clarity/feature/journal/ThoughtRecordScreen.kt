package com.runtimelabs.clarity.feature.journal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.BreathingIndicator
import com.runtimelabs.clarity.core.designsystem.components.ClarityPrimaryButton
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import kotlin.math.roundToInt

/** Situation -> automatic thought -> feeling + intensity -> reframe. */
@Composable
fun ThoughtRecordScreen(
    onDone: () -> Unit,
    viewModel: ThoughtRecordViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) onDone()
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = MaterialTheme.spacing.sm),
            ) {
                IconButton(onClick = onDone) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
                Text(
                    text = stringResource(R.string.journal_kind_thought),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (state.isExisting) {
                    IconButton(onClick = viewModel::onDeleteRequested) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.cd_delete_entry),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

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
                    LabeledField(
                        labelRes = R.string.thought_situation_label,
                        value = state.situation,
                        onValueChange = viewModel::onSituationChanged,
                        hintRes = R.string.thought_situation_hint,
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.lg))
                    LabeledField(
                        labelRes = R.string.thought_thought_label,
                        value = state.automaticThought,
                        onValueChange = viewModel::onThoughtChanged,
                        hintRes = R.string.thought_thought_hint,
                        minLines = 2,
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.lg))
                    LabeledField(
                        labelRes = R.string.thought_feeling_label,
                        value = state.feeling,
                        onValueChange = viewModel::onFeelingChanged,
                        hintRes = R.string.thought_feeling_hint,
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.md))
                    Text(
                        text = stringResource(R.string.thought_intensity_label, state.feelingIntensity),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Slider(
                        value = state.feelingIntensity.toFloat(),
                        onValueChange = { viewModel.onIntensityChanged(it.roundToInt()) },
                        valueRange = 0f..10f,
                        steps = 9,
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.md))
                    LabeledField(
                        labelRes = R.string.thought_reframe_label,
                        value = state.reframe,
                        onValueChange = viewModel::onReframeChanged,
                        hintRes = R.string.thought_reframe_hint,
                        minLines = 3,
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.xl))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.screenHorizontal)
                    .padding(bottom = MaterialTheme.spacing.lg),
            ) {
                ClarityPrimaryButton(
                    text = stringResource(R.string.thought_save),
                    onClick = viewModel::onSave,
                    enabled = state.canSave && !state.isLoading,
                    loading = state.isSaving,
                )
            }
        }
    }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteDismissed,
            title = { Text(stringResource(R.string.thought_delete_title)) },
            text = { Text(stringResource(R.string.journal_delete_message)) },
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

@Composable
private fun LabeledField(
    labelRes: Int,
    value: String,
    onValueChange: (String) -> Unit,
    hintRes: Int,
    minLines: Int = 1,
) {
    Column {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(stringResource(hintRes)) },
            minLines = minLines,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

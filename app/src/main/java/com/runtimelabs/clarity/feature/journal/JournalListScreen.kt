package com.runtimelabs.clarity.feature.journal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.ClarityCard
import com.runtimelabs.clarity.core.designsystem.components.EmptyState
import com.runtimelabs.clarity.core.designsystem.components.LoadingScreen
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.domain.model.JournalEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun JournalListScreen(
    onOpenEntry: (Long) -> Unit,
    onNewEntry: () -> Unit,
    viewModel: JournalViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val current = state) {
        JournalUiState.Loading -> LoadingScreen()
        is JournalUiState.Ready -> JournalListContent(
            entries = current.entries,
            onOpenEntry = onOpenEntry,
            onNewEntry = onNewEntry,
        )
    }
}

@Composable
private fun JournalListContent(
    entries: List<JournalEntry>,
    onOpenEntry: (Long) -> Unit,
    onNewEntry: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (entries.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.EditNote,
                title = stringResource(R.string.journal_empty_title),
                description = stringResource(R.string.journal_empty_description),
                actionLabel = stringResource(R.string.journal_new_entry),
                onAction = onNewEntry,
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = MaterialTheme.spacing.screenHorizontal,
                    end = MaterialTheme.spacing.screenHorizontal,
                    top = MaterialTheme.spacing.md,
                    bottom = 96.dp, // clearance for the FAB
                ),
            ) {
                item {
                    Column {
                        Text(
                            text = stringResource(R.string.nav_journal),
                            style = MaterialTheme.typography.headlineLarge,
                        )
                        Spacer(Modifier.height(MaterialTheme.spacing.md))
                    }
                }
                items(entries, key = { it.id }) { entry ->
                    JournalEntryCard(
                        entry = entry,
                        onClick = { onOpenEntry(entry.id) },
                        modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm),
                    )
                }
            }
            ExtendedFloatingActionButton(
                onClick = onNewEntry,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.journal_new_entry)) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(MaterialTheme.spacing.lg),
            )
        }
    }
}

@Composable
private fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateLabel = remember(entry.epochDay) {
        LocalDate.ofEpochDay(entry.epochDay)
            .format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault()))
    }
    ClarityCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        Text(
            text = entry.body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

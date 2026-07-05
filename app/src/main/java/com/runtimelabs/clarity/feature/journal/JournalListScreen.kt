package com.runtimelabs.clarity.feature.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalListScreen(
    onOpenEntry: (JournalHubEntry) -> Unit,
    onNewEntry: (JournalEntryKind) -> Unit,
    viewModel: JournalViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showChooser by remember { mutableStateOf(false) }

    when (val current = state) {
        JournalUiState.Loading -> LoadingScreen()
        is JournalUiState.Ready -> JournalListContent(
            entries = current.entries,
            onOpenEntry = onOpenEntry,
            onNewEntry = { showChooser = true },
        )
    }

    if (showChooser) {
        ModalBottomSheet(
            onDismissRequest = { showChooser = false },
            sheetState = rememberModalBottomSheetState(),
        ) {
            NewEntryChooser(
                onChosen = { kind ->
                    showChooser = false
                    onNewEntry(kind)
                },
            )
        }
    }
}

@Composable
private fun JournalListContent(
    entries: List<JournalHubEntry>,
    onOpenEntry: (JournalHubEntry) -> Unit,
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
                items(entries, key = { "${it.kind}:${it.id}" }) { entry ->
                    JournalHubRow(
                        entry = entry,
                        onClick = { onOpenEntry(entry) },
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

private fun JournalEntryKind.icon(): ImageVector = when (this) {
    JournalEntryKind.FREE -> Icons.Rounded.EditNote
    JournalEntryKind.THOUGHT -> Icons.Rounded.Psychology
    JournalEntryKind.GRATITUDE -> Icons.Rounded.Favorite
}

@Composable
private fun kindLabel(kind: JournalEntryKind): String = when (kind) {
    JournalEntryKind.FREE -> stringResource(R.string.journal_kind_free)
    JournalEntryKind.THOUGHT -> stringResource(R.string.journal_kind_thought)
    JournalEntryKind.GRATITUDE -> stringResource(R.string.journal_kind_gratitude)
}

@Composable
private fun JournalHubRow(
    entry: JournalHubEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateLabel = remember(entry.epochDay) {
        LocalDate.ofEpochDay(entry.epochDay)
            .format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))
    }
    ClarityCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
            ) {
                Icon(
                    imageVector = entry.kind.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(MaterialTheme.spacing.md))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = kindLabel(entry.kind),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = entry.preview,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun NewEntryChooser(onChosen: (JournalEntryKind) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.screenHorizontal)
            .navigationBarsPadding(),
    ) {
        Text(
            text = stringResource(R.string.journal_new_sheet_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        ChooserRow(
            icon = Icons.Rounded.EditNote,
            title = stringResource(R.string.journal_kind_free),
            description = stringResource(R.string.journal_new_free_desc),
            onClick = { onChosen(JournalEntryKind.FREE) },
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        ChooserRow(
            icon = Icons.Rounded.Psychology,
            title = stringResource(R.string.journal_kind_thought),
            description = stringResource(R.string.journal_new_thought_desc),
            onClick = { onChosen(JournalEntryKind.THOUGHT) },
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        ChooserRow(
            icon = Icons.Rounded.Favorite,
            title = stringResource(R.string.journal_kind_gratitude),
            description = stringResource(R.string.journal_new_gratitude_desc),
            onClick = { onChosen(JournalEntryKind.GRATITUDE) },
        )
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
    }
}

@Composable
private fun ChooserRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    ClarityCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(MaterialTheme.spacing.md))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

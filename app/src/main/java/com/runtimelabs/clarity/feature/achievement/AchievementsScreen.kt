package com.runtimelabs.clarity.feature.achievement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.LoadingScreen
import com.runtimelabs.clarity.core.designsystem.theme.extended
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.domain.badge.Badge
import com.runtimelabs.clarity.domain.badge.BadgeCategory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * The badge collection: every [Badge] the app defines, grouped by
 * [BadgeCategory], grid-laid-out, locked ones shown dimmed with a lock
 * glyph rather than hidden — seeing what's still ahead is part of what
 * makes a collection worth completing. Reached from a trophy icon on Home
 * (same "small icon on Home, not a bottom-bar tab" pattern Settings already
 * established — ARCHITECTURE.md §24) and previewed from Journey.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    viewModel: AchievementsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
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
                text = stringResource(R.string.achievements_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = MaterialTheme.spacing.xs),
            )
        }

        when (val current = state) {
            AchievementsUiState.Loading -> LoadingScreen()
            is AchievementsUiState.Ready -> {
                AchievementsGrid(state = current, onBadgeTapped = viewModel::onBadgeTapped)
                val selected = current.selected
                if (selected != null) {
                    ModalBottomSheet(
                        onDismissRequest = viewModel::onDetailDismissed,
                        sheetState = rememberModalBottomSheetState(),
                    ) {
                        BadgeDetailContent(badge = selected)
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementsGrid(
    state: AchievementsUiState.Ready,
    onBadgeTapped: (Badge) -> Unit,
) {
    val grouped = remember(state.badges) {
        state.badges.groupBy { it.badge.category }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.screenHorizontal,
            vertical = MaterialTheme.spacing.md,
        ),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = stringResource(R.string.achievements_progress, state.unlockedCount, state.totalCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm),
            )
        }
        CATEGORY_ORDER.forEach { category ->
            val badgesInCategory = grouped[category].orEmpty()
            if (badgesInCategory.isEmpty()) return@forEach
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(category.titleRes()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.sm, bottom = MaterialTheme.spacing.xs),
                )
            }
            items(badgesInCategory, key = { it.badge.storageValue }) { badgeUi ->
                BadgeTile(badgeUi = badgeUi, onClick = { onBadgeTapped(badgeUi.badge) })
            }
        }
    }
}

@Composable
private fun BadgeTile(badgeUi: BadgeUiModel, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
    ) {
        BadgeIcon(icon = badgeUi.badge.icon(), unlocked = badgeUi.isUnlocked, size = 64.dp)
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        Text(
            text = stringResource(badgeUi.badge.titleRes()),
            style = MaterialTheme.typography.labelSmall,
            color = if (badgeUi.isUnlocked) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
private fun BadgeIcon(icon: ImageVector, unlocked: Boolean, size: Dp) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .background(
                if (unlocked) {
                    MaterialTheme.extended.celebration.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                CircleShape,
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (unlocked) {
                MaterialTheme.extended.celebration
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            },
            modifier = Modifier.size(size * 0.45f),
        )
        if (!unlocked) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.4f)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = stringResource(R.string.achievement_locked),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(size * 0.22f),
                )
            }
        }
    }
}

@Composable
private fun BadgeDetailContent(badge: BadgeUiModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.lg)
            .padding(bottom = MaterialTheme.spacing.xxl),
    ) {
        BadgeIcon(icon = badge.badge.icon(), unlocked = badge.isUnlocked, size = 72.dp)
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text(
            text = stringResource(badge.badge.titleRes()),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        Text(
            text = stringResource(badge.badge.descriptionRes()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        if (badge.isUnlocked) {
            val dateLabel = remember(badge.unlockedAtEpochDay) {
                badge.unlockedAtEpochDay?.let {
                    LocalDate.ofEpochDay(it).format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))
                }
            }
            if (dateLabel != null) {
                Text(
                    text = stringResource(R.string.achievement_earned_on, dateLabel),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.extended.celebration,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.md))
            }
            Text(
                text = stringResource(badge.badge.unlockQuoteRes()),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text = stringResource(R.string.achievement_locked_hint),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val CATEGORY_ORDER = listOf(BadgeCategory.STREAK, BadgeCategory.RECOVERY, BadgeCategory.DAILY_PRACTICE)

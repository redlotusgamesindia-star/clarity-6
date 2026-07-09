package com.runtimelabs.clarity.feature.home

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.ads.ClarityBannerAd
import com.runtimelabs.clarity.ads.PrivacyOptionsViewModel
import com.runtimelabs.clarity.core.designsystem.components.LoadingScreen
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.core.util.rememberReduceMotionEnabled
import com.runtimelabs.clarity.domain.ads.AdScreen
import com.runtimelabs.clarity.feature.achievement.AchievementUnlockOverlay
import com.runtimelabs.clarity.feature.recovery.RelapseConfirmDialog
import com.runtimelabs.clarity.feature.recovery.textRes
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * The dashboard: greeting, streak ring, daily check-in, week of moods,
 * today's reflection. One scroll, no tabs-within-tabs — the streak is the
 * hero and everything else supports it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRecoveryFlow: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val reduceMotion = rememberReduceMotionEnabled()

    when (val current = state) {
        HomeUiState.Loading -> LoadingScreen()
        is HomeUiState.Ready -> {
            LaunchedEffect(current.pendingRecoveryFlowEventId) {
                val id = current.pendingRecoveryFlowEventId
                if (id != null) {
                    onNavigateToRecoveryFlow(id)
                    viewModel.onRecoveryFlowNavigated()
                }
            }

            HomeContent(
                state = current,
                reduceMotion = reduceMotion,
                onOpenCheckIn = viewModel::onOpenCheckIn,
                onRelapseButtonTapped = viewModel::onRelapseButtonTapped,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToAchievements = onNavigateToAchievements,
            )
            val sheet = current.checkInSheet
            if (sheet != null) {
                ModalBottomSheet(
                    onDismissRequest = viewModel::onDismissCheckIn,
                    sheetState = rememberModalBottomSheetState(),
                ) {
                    CheckInSheetContent(
                        state = sheet,
                        onMoodSelected = viewModel::onMoodSelected,
                        onUrgeChanged = viewModel::onUrgeChanged,
                        onSave = viewModel::onSaveCheckIn,
                    )
                }
            }
            if (current.showRelapseConfirm) {
                RelapseConfirmDialog(
                    onConfirm = viewModel::onRelapseConfirmed,
                    onDismiss = viewModel::onRelapseDialogDismissed,
                )
            }
            AchievementUnlockOverlay(
                newlyUnlocked = current.newlyUnlockedBadges,
                reduceMotion = reduceMotion,
                onDismiss = viewModel::onAchievementCelebrationDismissed,
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Ready,
    reduceMotion: Boolean,
    onOpenCheckIn: () -> Unit,
    onRelapseButtonTapped: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAchievements: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
    ) {
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Row(verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.weight(1f)) {
                GreetingHeader(todayEpochDay = state.todayEpochDay)
            }
            IconButton(onClick = onNavigateToAchievements) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = stringResource(R.string.achievements_title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(R.string.settings_title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            StreakRing(
                currentDays = state.streak.currentDays,
                milestoneDays = state.milestoneDays,
                reduceMotion = reduceMotion,
                isRebuilding = state.streak.isRebuilding,
                hasBeatenPreviousRecord = state.streak.hasBeatenPreviousRecord,
            )
        }
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        StreakCaptions(state)

        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        RelapseEntryLink(onClick = onRelapseButtonTapped)

        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        CheckInCard(todayCheckIn = state.todayCheckIn, onClick = onOpenCheckIn)

        Spacer(Modifier.height(MaterialTheme.spacing.md))
        MoodWeekCard(weekCheckIns = state.weekCheckIns, todayEpochDay = state.todayEpochDay)

        Spacer(Modifier.height(MaterialTheme.spacing.md))
        QuoteCard(todayEpochDay = state.todayEpochDay)

        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        ClarityBannerAd(screen = AdScreen.HOME)

        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        PrivacyOptionsLink()

        Spacer(Modifier.height(MaterialTheme.spacing.xxl))
    }
}

/**
 * Deliberately small and quiet — a text link, not a button. This sits next
 * to the streak every single day; a prominent always-visible "relapse" CTA
 * would itself work against the whole point of this feature (§22). Findable
 * in one tap, but never the loudest thing on the screen.
 */
@Composable
private fun RelapseEntryLink(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // 48dp minimum touch target (Android accessibility guidance) —
            // the label itself stays visually small and quiet on purpose
            // (see the doc comment above), the tappable area doesn't have to.
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.relapse_entry_link),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = TextDecoration.Underline,
        )
    }
}

/**
 * Only rendered when UMP reports a privacy-options entry point is actually
 * required — matching UMP's own documented guidance to keep this invisible
 * otherwise. There is no Settings screen in this app to put this in yet, so
 * it lives here, next to the other small quiet link on this screen.
 */
@Composable
private fun PrivacyOptionsLink(viewModel: PrivacyOptionsViewModel = hiltViewModel()) {
    val isRequired by viewModel.isPrivacyOptionsRequired.collectAsStateWithLifecycle()
    if (!isRequired) return

    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable {
                (context as? Activity)?.let { activity ->
                    viewModel.showPrivacyOptionsForm(activity) { }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.privacy_options_link),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = TextDecoration.Underline,
        )
    }
}

@Composable
private fun GreetingHeader(todayEpochDay: Long) {
    val greetingRes = remember {
        val hour = LocalTime.now().hour
        when {
            hour < 12 -> R.string.home_greeting_morning
            hour < 17 -> R.string.home_greeting_afternoon
            else -> R.string.home_greeting_evening
        }
    }
    val dateLabel = remember(todayEpochDay) {
        LocalDate.ofEpochDay(todayEpochDay)
            .format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
    }
    Text(
        text = stringResource(greetingRes),
        style = MaterialTheme.typography.headlineLarge,
    )
    Spacer(Modifier.height(MaterialTheme.spacing.xs))
    Text(
        text = dateLabel,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun StreakCaptions(state: HomeUiState.Ready) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        when {
            state.streak.isRebuilding -> {
                // Rebuild System (§22): "Recovery Day N" already lives on
                // the ring itself; here we show the two numbers that prove
                // the earlier progress wasn't erased.
                state.streak.previousRunDays?.let { previous ->
                    Text(
                        text = stringResource(R.string.home_previous_streak, previous),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.xs))
                }
                Text(
                    text = stringResource(R.string.home_best_streak, state.streak.longestDays),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
            }
            state.streak.currentDays >= state.milestoneDays -> {
                Text(
                    text = stringResource(R.string.home_milestone_reached),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center,
                )
            }
            else -> {
                Text(
                    text = stringResource(R.string.plan_first_milestone, state.milestoneDays),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (!state.streak.isRebuilding && state.streak.longestDays > state.streak.currentDays) {
            Spacer(Modifier.height(MaterialTheme.spacing.xs))
            Text(
                text = stringResource(R.string.home_longest_streak, state.streak.longestDays),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        state.motivationMessage?.let { message ->
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Text(
                text = stringResource(message.textRes()),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

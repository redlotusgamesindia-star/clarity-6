package com.runtimelabs.clarity.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.LoadingScreen
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.core.util.rememberReduceMotionEnabled
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
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val reduceMotion = rememberReduceMotionEnabled()

    when (val current = state) {
        HomeUiState.Loading -> LoadingScreen()
        is HomeUiState.Ready -> {
            HomeContent(
                state = current,
                reduceMotion = reduceMotion,
                onOpenCheckIn = viewModel::onOpenCheckIn,
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
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Ready,
    reduceMotion: Boolean,
    onOpenCheckIn: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
    ) {
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        GreetingHeader(todayEpochDay = state.todayEpochDay)

        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            StreakRing(
                currentDays = state.streak.currentDays,
                milestoneDays = state.milestoneDays,
                reduceMotion = reduceMotion,
            )
        }
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        StreakCaptions(state)

        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        CheckInCard(todayCheckIn = state.todayCheckIn, onClick = onOpenCheckIn)

        Spacer(Modifier.height(MaterialTheme.spacing.md))
        MoodWeekCard(weekCheckIns = state.weekCheckIns, todayEpochDay = state.todayEpochDay)

        Spacer(Modifier.height(MaterialTheme.spacing.md))
        QuoteCard(todayEpochDay = state.todayEpochDay)

        Spacer(Modifier.height(MaterialTheme.spacing.xxl))
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
        if (state.streak.currentDays >= state.milestoneDays) {
            Text(
                text = stringResource(R.string.home_milestone_reached),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text = stringResource(R.string.plan_first_milestone, state.milestoneDays),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (state.streak.longestDays > state.streak.currentDays) {
            Spacer(Modifier.height(MaterialTheme.spacing.xs))
            Text(
                text = stringResource(R.string.home_longest_streak, state.streak.longestDays),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

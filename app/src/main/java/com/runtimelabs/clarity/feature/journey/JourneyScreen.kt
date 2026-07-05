package com.runtimelabs.clarity.feature.journey

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.ads.ClarityBannerAd
import com.runtimelabs.clarity.core.designsystem.components.ClarityCard
import com.runtimelabs.clarity.core.designsystem.components.EmptyState
import com.runtimelabs.clarity.core.designsystem.components.LoadingScreen
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.core.util.rememberReduceMotionEnabled
import com.runtimelabs.clarity.domain.ads.AdScreen

@Composable
fun JourneyScreen(
    onNewHabit: () -> Unit,
    onEditHabit: (Long) -> Unit,
    viewModel: JourneyViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val reduceMotion = rememberReduceMotionEnabled()

    when (val current = state) {
        JourneyUiState.Loading -> LoadingScreen()
        is JourneyUiState.Ready -> JourneyContent(
            state = current,
            reduceMotion = reduceMotion,
            onNewHabit = onNewHabit,
            onEditHabit = onEditHabit,
            onToggleToday = viewModel::onToggleToday,
        )
    }
}

@Composable
private fun JourneyContent(
    state: JourneyUiState.Ready,
    reduceMotion: Boolean,
    onNewHabit: () -> Unit,
    onEditHabit: (Long) -> Unit,
    onToggleToday: (Long, Boolean) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
        ) {
            Spacer(Modifier.height(MaterialTheme.spacing.md))
            Text(
                text = stringResource(R.string.nav_journey),
                style = MaterialTheme.typography.headlineLarge,
            )

            // Recovery Score and its achievements are about the streak/
            // relapse journey specifically — unrelated to whether any habit
            // has been set up yet, so they always render here rather than
            // living behind the habit empty-state below.
            Spacer(Modifier.height(MaterialTheme.spacing.lg))
            RecoveryScoreCard(score = state.recoveryScore)
            if (state.comebackAchievements.isNotEmpty()) {
                Spacer(Modifier.height(MaterialTheme.spacing.md))
                ComebackAchievementsSection(achievements = state.comebackAchievements)
            }

            if (!state.hasHabits) {
                Spacer(Modifier.height(MaterialTheme.spacing.xl))
                EmptyState(
                    icon = Icons.Rounded.TaskAlt,
                    title = stringResource(R.string.journey_empty_title),
                    description = stringResource(R.string.journey_empty_description),
                    actionLabel = stringResource(R.string.habit_new),
                    onAction = onNewHabit,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.xxl))
                return@Column
            }

            Spacer(Modifier.height(MaterialTheme.spacing.lg))
            Text(
                text = stringResource(R.string.journey_today),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            ClarityCard {
                if (state.todaysHabits.isEmpty()) {
                    Text(
                        text = stringResource(R.string.journey_rest_today),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.todaysHabits.forEach { status ->
                        HabitTodayRow(
                            status = status,
                            onToggle = { done -> onToggleToday(status.habit.id, done) },
                            onOpen = { onEditHabit(status.habit.id) },
                        )
                    }
                }
            }

            if (state.otherHabits.isNotEmpty()) {
                Spacer(Modifier.height(MaterialTheme.spacing.lg))
                Text(
                    text = stringResource(R.string.journey_other_days),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                ClarityCard {
                    state.otherHabits.forEach { habit ->
                        HabitOtherDayRow(habit = habit, onOpen = { onEditHabit(habit.id) })
                    }
                }
            }

            Spacer(Modifier.height(MaterialTheme.spacing.lg))
            Text(
                text = stringResource(R.string.journey_this_week),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            ClarityCard {
                WeekBarChart(days = state.weekDays, reduceMotion = reduceMotion)
                Spacer(Modifier.height(MaterialTheme.spacing.md))
                Text(
                    text = stringResource(
                        R.string.journey_week_summary,
                        state.weekCompleted,
                        state.weekScheduled,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(MaterialTheme.spacing.lg))
            Text(
                text = stringResource(R.string.journey_insights_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            ClarityCard {
                state.insights.forEachIndexed { index, insight ->
                    if (index > 0) Spacer(Modifier.height(MaterialTheme.spacing.md))
                    InsightRow(insight = insight)
                }
            }

            Spacer(Modifier.height(MaterialTheme.spacing.lg))
            ClarityBannerAd(screen = AdScreen.JOURNEY)

            Spacer(Modifier.height(96.dp)) // FAB clearance
        }

        if (state.hasHabits) {
            ExtendedFloatingActionButton(
                onClick = onNewHabit,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.habit_new)) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(MaterialTheme.spacing.lg),
            )
        }
    }
}

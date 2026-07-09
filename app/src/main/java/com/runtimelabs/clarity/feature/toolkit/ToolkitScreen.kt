package com.runtimelabs.clarity.feature.toolkit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.BreathingIndicator
import com.runtimelabs.clarity.core.designsystem.components.ClarityCard
import com.runtimelabs.clarity.core.designsystem.components.ClarityPrimaryButton
import com.runtimelabs.clarity.core.designsystem.theme.MotionTokens
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.core.util.rememberReduceMotionEnabled

/**
 * The SOS destination — the dawn-amber button finally has its purpose.
 * Breathing is the hero (one tap, auto-starts); everything else is grouped
 * into scannable sections rather than one long flat list, since this grew
 * from 5 tools to 14. Pushed on top of whatever tab the user was in, so
 * leaving here returns them exactly where they were.
 *
 * Grounding, Muscle Relaxation, and Quick Reframe are the original three
 * tools from before this screen's expansion — kept rather than replaced,
 * since they're real, evidence-based techniques with content behind them,
 * not placeholders (ARCHITECTURE.md §29).
 */
@Composable
fun ToolkitScreen(
    onBack: () -> Unit,
    onBreathe: () -> Unit,
    onBreatheTimed: (Int) -> Unit,
    onColdShower: () -> Unit,
    onWalkOutside: () -> Unit,
    onPushUps: () -> Unit,
    onDrinkWater: () -> Unit,
    onGrounding: () -> Unit,
    onMuscle: () -> Unit,
    onReframe: () -> Unit,
    onCallFriend: () -> Unit,
    onWriteJournal: () -> Unit,
    onWhy: () -> Unit,
    onDistractionIdeas: () -> Unit,
) {
    val reduceMotion = rememberReduceMotionEnabled()

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
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
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
            ) {
                Text(
                    text = stringResource(R.string.toolkit_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.xs))
                Text(
                    text = stringResource(R.string.toolkit_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(MaterialTheme.spacing.xl))
                SectionLabel(R.string.toolkit_section_breathe)
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                EntranceItem(index = 0, reduceMotion = reduceMotion) {
                    BreathingHeroCard(onBreathe = onBreathe)
                }
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                EntranceItem(index = 1, reduceMotion = reduceMotion) {
                    TimedBreathingRow(onBreatheTimed = onBreatheTimed)
                }

                Spacer(Modifier.height(MaterialTheme.spacing.lg))
                SectionLabel(R.string.toolkit_section_reset_body)
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                listOf(
                    Triple(Icons.Rounded.AcUnit, R.string.tool_cold_shower_title, R.string.tool_cold_shower_subtitle) to onColdShower,
                    Triple(Icons.Rounded.DirectionsWalk, R.string.tool_walk_outside_title, R.string.tool_walk_outside_subtitle) to onWalkOutside,
                    Triple(Icons.Rounded.FitnessCenter, R.string.tool_push_ups_title, R.string.tool_push_ups_subtitle) to onPushUps,
                    Triple(Icons.Rounded.LocalDrink, R.string.tool_drink_water_title, R.string.tool_drink_water_subtitle) to onDrinkWater,
                ).forEachIndexed { i, (content, onClick) ->
                    val (icon, titleRes, subtitleRes) = content
                    EntranceItem(index = 2 + i, reduceMotion = reduceMotion) {
                        ToolCard(icon = icon, titleRes = titleRes, subtitleRes = subtitleRes, onClick = onClick)
                    }
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                }

                Spacer(Modifier.height(MaterialTheme.spacing.md))
                SectionLabel(R.string.toolkit_section_ground_reframe)
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                listOf(
                    Triple(Icons.Rounded.Visibility, R.string.toolkit_grounding_title, R.string.toolkit_grounding_subtitle) to onGrounding,
                    Triple(Icons.Rounded.SelfImprovement, R.string.toolkit_muscle_title, R.string.toolkit_muscle_subtitle) to onMuscle,
                    Triple(Icons.Rounded.Psychology, R.string.toolkit_reframe_title, R.string.toolkit_reframe_subtitle) to onReframe,
                ).forEachIndexed { i, (content, onClick) ->
                    val (icon, titleRes, subtitleRes) = content
                    EntranceItem(index = 6 + i, reduceMotion = reduceMotion) {
                        ToolCard(icon = icon, titleRes = titleRes, subtitleRes = subtitleRes, onClick = onClick)
                    }
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                }

                Spacer(Modifier.height(MaterialTheme.spacing.md))
                SectionLabel(R.string.toolkit_section_reach_reflect)
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                listOf(
                    Triple(Icons.Rounded.Call, R.string.tool_call_friend_title, R.string.tool_call_friend_subtitle) to onCallFriend,
                    Triple(Icons.Rounded.Edit, R.string.tool_write_journal_title, R.string.tool_write_journal_subtitle) to onWriteJournal,
                    Triple(Icons.Rounded.Favorite, R.string.tool_motivation_wall_title, R.string.tool_motivation_wall_subtitle) to onWhy,
                ).forEachIndexed { i, (content, onClick) ->
                    val (icon, titleRes, subtitleRes) = content
                    EntranceItem(index = 9 + i, reduceMotion = reduceMotion) {
                        ToolCard(icon = icon, titleRes = titleRes, subtitleRes = subtitleRes, onClick = onClick)
                    }
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                }

                Spacer(Modifier.height(MaterialTheme.spacing.md))
                SectionLabel(R.string.toolkit_section_distract)
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                EntranceItem(index = 12, reduceMotion = reduceMotion) {
                    ToolCard(
                        icon = Icons.Rounded.Lightbulb,
                        titleRes = R.string.tool_distraction_ideas_title,
                        subtitleRes = R.string.tool_distraction_ideas_subtitle,
                        onClick = onDistractionIdeas,
                    )
                }

                Spacer(Modifier.height(MaterialTheme.spacing.lg))
                Text(
                    text = stringResource(R.string.toolkit_disclaimer),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(MaterialTheme.spacing.xl))
            }
        }
    }
}

@Composable
private fun SectionLabel(textRes: Int) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun BreathingHeroCard(onBreathe: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BreathingIndicator(size = 40.dp)
                Spacer(Modifier.width(MaterialTheme.spacing.md))
                Column {
                    Text(
                        text = stringResource(R.string.toolkit_breathe_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.toolkit_breathe_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(MaterialTheme.spacing.md))
            ClarityPrimaryButton(
                text = stringResource(R.string.toolkit_breathe_action),
                onClick = onBreathe,
            )
        }
    }
}

@Composable
private fun TimedBreathingRow(onBreatheTimed: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm), modifier = Modifier.fillMaxWidth()) {
        TimedBreathingChip(R.string.tool_breathing_30s_title, 30, Modifier.weight(1f), onBreatheTimed)
        TimedBreathingChip(R.string.tool_breathing_60s_title, 60, Modifier.weight(1f), onBreatheTimed)
        TimedBreathingChip(R.string.tool_breathing_2min_title, 120, Modifier.weight(1f), onBreatheTimed)
    }
}

@Composable
private fun TimedBreathingChip(
    labelRes: Int,
    seconds: Int,
    modifier: Modifier,
    onBreatheTimed: (Int) -> Unit,
) {
    Surface(
        onClick = { onBreatheTimed(seconds) },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.spacing.sm, horizontal = MaterialTheme.spacing.xs),
        )
    }
}

/** One beat in the staggered entrance; index 0 leads, each later index follows by a fixed offset, capped so a long list doesn't feel sluggish by its final item. */
@Composable
private fun EntranceItem(
    index: Int,
    reduceMotion: Boolean,
    content: @Composable () -> Unit,
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val cappedIndex = index.coerceAtMost(MAX_STAGGER_INDEX)
    val beat by animateFloatAsState(
        targetValue = if (started || reduceMotion) 1f else 0f,
        animationSpec = tween(
            durationMillis = MotionTokens.EMPHASIZED,
            delayMillis = if (reduceMotion) 0 else cappedIndex * STAGGER_DELAY_MS,
        ),
        label = "toolkitEntranceBeat$index",
    )
    Box(
        modifier = Modifier.graphicsLayer {
            alpha = beat
            translationY = (1f - beat) * 16.dp.toPx()
        },
    ) {
        content()
    }
}

private const val STAGGER_DELAY_MS = 60
private const val MAX_STAGGER_INDEX = 6

@Composable
private fun ToolCard(
    icon: ImageVector,
    titleRes: Int,
    subtitleRes: Int,
    onClick: () -> Unit,
) {
    ClarityCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(MaterialTheme.spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.hairline))
                Text(
                    text = stringResource(subtitleRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

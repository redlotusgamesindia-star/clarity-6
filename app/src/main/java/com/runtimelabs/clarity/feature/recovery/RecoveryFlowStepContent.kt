package com.runtimelabs.clarity.feature.recovery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.ClarityCard
import com.runtimelabs.clarity.core.designsystem.theme.extended
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.domain.model.RelapseEmotion
import com.runtimelabs.clarity.domain.model.RelapseSetbackType
import com.runtimelabs.clarity.domain.model.RelapseTrigger
import com.runtimelabs.clarity.domain.recovery.RecoveryChecklistItem
import com.runtimelabs.clarity.domain.recovery.RecoveryChecklistItemCode
import com.runtimelabs.clarity.feature.onboarding.OptionCard

// ---------------------------------------------------------------- accept --

@Composable
fun AcceptContent(previousRunDays: Int?, bestStreakDays: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.xl),
    ) {
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = Icons.Rounded.Spa,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        Text(
            text = stringResource(R.string.recovery_accept_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text(
            text = stringResource(R.string.recovery_accept_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
            if (previousRunDays != null) {
                StatBadge(
                    value = previousRunDays,
                    labelRes = R.string.recovery_previous_streak_label,
                    modifier = Modifier.weight(1f),
                )
            }
            StatBadge(
                value = bestStreakDays,
                labelRes = R.string.recovery_best_streak_label,
                emphasized = true,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun StatBadge(
    value: Int,
    labelRes: Int,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    ClarityCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineLarge,
                color = if (emphasized) MaterialTheme.extended.celebration else MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ------------------------------------------------------- single-question --

/**
 * The shared shape behind What Happened / Feelings / Trigger: a title, a
 * subtitle, and a single-select list of [OptionCard]s. None of the three
 * force a selection to continue — matching this whole flow's standing
 * principle that reflection is offered, never required, even though each
 * is now its own focused step rather than one long scrollable form.
 */
@Composable
private fun <T> SingleQuestionContent(
    titleRes: Int,
    subtitleRes: Int,
    options: List<T>,
    selected: T?,
    labelRes: (T) -> Int,
    onSelected: (T) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
    ) {
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text(text = stringResource(titleRes), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        Text(
            text = stringResource(subtitleRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        options.forEach { option ->
            OptionCard(
                text = stringResource(labelRes(option)),
                selected = selected == option,
                onClick = { onSelected(option) },
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm),
            )
        }
        Spacer(Modifier.height(MaterialTheme.spacing.xxl))
    }
}

@Composable
fun WhatHappenedContent(selected: RelapseSetbackType?, onSelected: (RelapseSetbackType) -> Unit) {
    SingleQuestionContent(
        titleRes = R.string.recovery_what_happened_title,
        subtitleRes = R.string.recovery_what_happened_subtitle,
        options = RelapseSetbackType.entries,
        selected = selected,
        labelRes = { it.labelRes() },
        onSelected = onSelected,
    )
}

@Composable
fun FeelingsContent(selected: RelapseEmotion?, onSelected: (RelapseEmotion) -> Unit) {
    SingleQuestionContent(
        titleRes = R.string.recovery_feelings_title,
        subtitleRes = R.string.recovery_feelings_subtitle,
        options = RelapseEmotion.entries,
        selected = selected,
        labelRes = { it.labelRes() },
        onSelected = onSelected,
    )
}

@Composable
fun TriggerContent(selected: RelapseTrigger?, onSelected: (RelapseTrigger) -> Unit) {
    SingleQuestionContent(
        titleRes = R.string.recovery_trigger_title,
        subtitleRes = R.string.recovery_trigger_subtitle,
        options = RelapseTrigger.entries,
        selected = selected,
        labelRes = { it.labelRes() },
        onSelected = onSelected,
    )
}

// ------------------------------------------------------------------ learn --

private data class RelapsePattern(
    val icon: ImageVector,
    val titleRes: Int,
    val descriptionRes: Int,
    val matchesTriggers: Set<RelapseTrigger>,
)

private val RELAPSE_PATTERNS = listOf(
    RelapsePattern(Icons.Rounded.SelfImprovement, R.string.pattern_boredom_title, R.string.pattern_boredom_desc, setOf(RelapseTrigger.BOREDOM)),
    RelapsePattern(Icons.Rounded.Spa, R.string.pattern_stress_title, R.string.pattern_stress_desc, setOf(RelapseTrigger.STRESS)),
    RelapsePattern(Icons.Rounded.People, R.string.pattern_loneliness_title, R.string.pattern_loneliness_desc, setOf(RelapseTrigger.LONELINESS)),
    RelapsePattern(Icons.Rounded.PhoneAndroid, R.string.pattern_social_media_title, R.string.pattern_social_media_desc, setOf(RelapseTrigger.SOCIAL_MEDIA)),
    RelapsePattern(Icons.Rounded.NightsStay, R.string.pattern_nighttime_title, R.string.pattern_nighttime_desc, setOf(RelapseTrigger.NIGHT, RelapseTrigger.COULDNT_SLEEP)),
)

@Composable
fun LearnContent(matchedTrigger: RelapseTrigger?) {
    val ordered = remember(matchedTrigger) {
        RELAPSE_PATTERNS.sortedByDescending { pattern -> matchedTrigger != null && matchedTrigger in pattern.matchesTriggers }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
    ) {
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text(
            text = stringResource(R.string.recovery_learn_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        Text(
            text = stringResource(R.string.recovery_learn_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        ordered.forEach { pattern ->
            val isMatch = matchedTrigger != null && matchedTrigger in pattern.matchesTriggers
            PatternCard(pattern = pattern, highlighted = isMatch)
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
        }
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
    }
}

@Composable
private fun PatternCard(pattern: RelapsePattern, highlighted: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (highlighted) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (highlighted) 1.5.dp else 1.dp,
            color = if (highlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            },
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(MaterialTheme.spacing.md)) {
            Icon(
                imageVector = pattern.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(MaterialTheme.spacing.md))
            Column {
                Text(text = stringResource(pattern.titleRes), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(pattern.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ------------------------------------------------------------------- plan --

@Composable
fun PlanContent(
    checklist: List<RecoveryChecklistItem>,
    checkedCodes: Set<RecoveryChecklistItemCode>,
    onToggle: (RecoveryChecklistItemCode) -> Unit,
    onOpenBreathing: () -> Unit,
    onOpenJournal: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
    ) {
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text(
            text = stringResource(R.string.recovery_plan_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        Text(
            text = stringResource(R.string.recovery_plan_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        checklist.forEach { item ->
            val deepLink = when (item.code) {
                RecoveryChecklistItemCode.BREATHING_EXERCISE -> onOpenBreathing
                RecoveryChecklistItemCode.JOURNAL_IT -> onOpenJournal
                else -> null
            }
            ChecklistRow(
                checked = item.code in checkedCodes,
                title = stringResource(item.code.titleRes()),
                onToggle = { onToggle(item.code) },
                onOpen = deepLink,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
        }
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
    }
}

@Composable
private fun ChecklistRow(
    checked: Boolean,
    title: String,
    onToggle: () -> Unit,
    onOpen: (() -> Unit)?,
) {
    ClarityCard(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                border = if (checked) {
                    null
                } else {
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                },
                modifier = Modifier.size(28.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    // Plain conditional, not AnimatedVisibility: this Box sits
                    // inside a Surface inside a Row, all within ChecklistRow's
                    // own body — Surface introduces no scope of its own, so
                    // RowScope stays lexically visible all the way down and
                    // collides with BoxScope at this call site. Function-call
                    // boundaries (like DoneToggle's) reset that; inline
                    // nesting within one function body does not.
                    if (checked) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.width(MaterialTheme.spacing.md))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (checked) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f),
            )
            if (onOpen != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = stringResource(R.string.checklist_open_tool),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onOpen),
                )
            }
        }
    }
}

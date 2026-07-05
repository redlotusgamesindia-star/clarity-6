package com.runtimelabs.clarity.feature.recovery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.material3.OutlinedTextField
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
import com.runtimelabs.clarity.core.designsystem.theme.MotionTokens
import com.runtimelabs.clarity.core.designsystem.theme.extended
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.domain.model.MainTrigger
import com.runtimelabs.clarity.domain.model.MoodLevel
import com.runtimelabs.clarity.domain.model.RelapseLocation
import com.runtimelabs.clarity.domain.model.UrgeTime
import com.runtimelabs.clarity.domain.recovery.RecoveryChecklistItem
import com.runtimelabs.clarity.domain.recovery.RecoveryChecklistItemCode
import com.runtimelabs.clarity.feature.home.labelRes
import com.runtimelabs.clarity.feature.onboarding.OptionCard
import com.runtimelabs.clarity.feature.onboarding.labelRes

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

// ------------------------------------------------------------ reflection --

data class ReflectionActions(
    val onTriggerSelected: (MainTrigger) -> Unit,
    val onTimeOfDaySelected: (UrgeTime) -> Unit,
    val onMoodSelected: (MoodLevel) -> Unit,
    val onLocationSelected: (RelapseLocation) -> Unit,
    val onNotesChanged: (String) -> Unit,
)

@Composable
fun ReflectionContent(draft: RecoveryFlowDraft, actions: ReflectionActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
    ) {
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text(
            text = stringResource(R.string.recovery_reflection_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        Text(
            text = stringResource(R.string.recovery_reflection_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        ReflectionSection(stringResource(R.string.recovery_reflection_trigger_label)) {
            MainTrigger.entries.forEach { trigger ->
                OptionCard(
                    text = stringResource(trigger.labelRes()),
                    selected = draft.trigger == trigger,
                    onClick = { actions.onTriggerSelected(trigger) },
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm),
                )
            }
        }

        ReflectionSection(stringResource(R.string.recovery_reflection_time_label)) {
            UrgeTime.entries.forEach { time ->
                OptionCard(
                    text = stringResource(time.labelRes()),
                    selected = draft.timeOfDay == time,
                    onClick = { actions.onTimeOfDaySelected(time) },
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm),
                )
            }
        }

        ReflectionSection(stringResource(R.string.recovery_reflection_mood_label)) {
            MoodLevel.entries.forEach { mood ->
                OptionCard(
                    text = stringResource(mood.labelRes()),
                    selected = draft.mood == mood,
                    onClick = { actions.onMoodSelected(mood) },
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm),
                )
            }
        }

        ReflectionSection(stringResource(R.string.recovery_reflection_location_label)) {
            RelapseLocation.entries.forEach { location ->
                OptionCard(
                    text = stringResource(location.labelRes()),
                    selected = draft.location == location,
                    onClick = { actions.onLocationSelected(location) },
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm),
                )
            }
        }

        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text(
            text = stringResource(R.string.recovery_reflection_notes_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        OutlinedTextField(
            value = draft.notes,
            onValueChange = actions.onNotesChanged,
            placeholder = { Text(stringResource(R.string.recovery_reflection_notes_hint)) },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xxl))
    }
}

@Composable
private fun ReflectionSection(label: String, content: @Composable () -> Unit) {
    Spacer(Modifier.height(MaterialTheme.spacing.lg))
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(MaterialTheme.spacing.sm))
    content()
}

// ------------------------------------------------------------------ learn --

private data class RelapsePattern(
    val icon: ImageVector,
    val titleRes: Int,
    val descriptionRes: Int,
    val matchesTrigger: MainTrigger?,
    val matchesLateNight: Boolean = false,
)

private val RELAPSE_PATTERNS = listOf(
    RelapsePattern(Icons.Rounded.SelfImprovement, R.string.pattern_boredom_title, R.string.pattern_boredom_desc, MainTrigger.BOREDOM),
    RelapsePattern(Icons.Rounded.Spa, R.string.pattern_stress_title, R.string.pattern_stress_desc, MainTrigger.STRESS),
    RelapsePattern(Icons.Rounded.People, R.string.pattern_loneliness_title, R.string.pattern_loneliness_desc, MainTrigger.LONELINESS),
    RelapsePattern(Icons.Rounded.PhoneAndroid, R.string.pattern_social_media_title, R.string.pattern_social_media_desc, MainTrigger.SOCIAL_MEDIA),
    RelapsePattern(Icons.Rounded.NightsStay, R.string.pattern_nighttime_title, R.string.pattern_nighttime_desc, null, matchesLateNight = true),
)

@Composable
fun LearnContent(matchedTrigger: MainTrigger?, matchedLateNight: Boolean) {
    val ordered = remember(matchedTrigger, matchedLateNight) {
        RELAPSE_PATTERNS.sortedByDescending { pattern ->
            (pattern.matchesTrigger != null && pattern.matchesTrigger == matchedTrigger) ||
                (pattern.matchesLateNight && matchedLateNight)
        }
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
            val isMatch = (pattern.matchesTrigger != null && pattern.matchesTrigger == matchedTrigger) ||
                (pattern.matchesLateNight && matchedLateNight)
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
                    AnimatedVisibility(
                        visible = checked,
                        enter = fadeIn(tween(MotionTokens.QUICK)) + scaleIn(initialScale = 0.6f),
                        exit = fadeOut(tween(MotionTokens.QUICK)),
                    ) {
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

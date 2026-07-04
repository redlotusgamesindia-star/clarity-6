package com.runtimelabs.clarity.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.ClarityPrimaryButton
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.domain.model.MoodLevel
import kotlin.math.roundToInt

/**
 * Ten seconds, two answers: how you feel, how loud the urges were. Mood is
 * required (it's the point); urge defaults to 0 so "no urges today" costs
 * nothing to report.
 */
@Composable
fun CheckInSheetContent(
    state: CheckInSheetState,
    onMoodSelected: (MoodLevel) -> Unit,
    onUrgeChanged: (Int) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.screenHorizontal)
            .navigationBarsPadding(),
    ) {
        Text(
            text = stringResource(R.string.checkin_sheet_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.lg))

        Text(
            text = stringResource(R.string.checkin_mood_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            MoodLevel.entries.forEach { mood ->
                MoodOption(
                    mood = mood,
                    selected = state.mood == mood,
                    onClick = { onMoodSelected(mood) },
                )
            }
        }
        Spacer(Modifier.height(MaterialTheme.spacing.lg))

        Text(
            text = stringResource(R.string.checkin_urge_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = state.urgeLevel.toFloat(),
            onValueChange = { onUrgeChanged(it.roundToInt()) },
            valueRange = 0f..10f,
            steps = 9,
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.checkin_urge_low),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.checkin_urge_high),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(MaterialTheme.spacing.lg))

        ClarityPrimaryButton(
            text = stringResource(R.string.checkin_save),
            onClick = onSave,
            enabled = state.mood != null,
            loading = state.isSaving,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
    }
}

@Composable
private fun MoodOption(
    mood: MoodLevel,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        },
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier.size(52.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = mood.icon(),
                contentDescription = stringResource(mood.labelRes()),
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

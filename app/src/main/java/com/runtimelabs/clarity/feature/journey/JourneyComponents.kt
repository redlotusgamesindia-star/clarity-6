package com.runtimelabs.clarity.feature.journey

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PhoneDisabled
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.domain.habit.DayStat
import com.runtimelabs.clarity.domain.insight.Insight
import com.runtimelabs.clarity.domain.insight.InsightCode
import com.runtimelabs.clarity.domain.model.Habit
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

// ------------------------------------------------------------ icon catalog --

/** Curated icon set: stable codes in the DB, vectors resolved here. */
val HABIT_ICON_CODES = listOf(
    "spa", "walk", "book", "water", "gym",
    "meditate", "sleep", "phone_off", "journal", "sun",
)

fun habitIcon(code: String): ImageVector = when (code) {
    "walk" -> Icons.Rounded.DirectionsWalk
    "book" -> Icons.Rounded.MenuBook
    "water" -> Icons.Rounded.WaterDrop
    "gym" -> Icons.Rounded.FitnessCenter
    "meditate" -> Icons.Rounded.SelfImprovement
    "sleep" -> Icons.Rounded.Bedtime
    "phone_off" -> Icons.Rounded.PhoneDisabled
    "journal" -> Icons.Rounded.EditNote
    "sun" -> Icons.Rounded.WbSunny
    else -> Icons.Rounded.Spa // "spa" + unknown codes degrade gracefully
}

@StringRes
fun habitIconLabelRes(code: String): Int = when (code) {
    "walk" -> R.string.icon_walk
    "book" -> R.string.icon_book
    "water" -> R.string.icon_water
    "gym" -> R.string.icon_gym
    "meditate" -> R.string.icon_meditate
    "sleep" -> R.string.icon_sleep
    "phone_off" -> R.string.icon_phone_off
    "journal" -> R.string.icon_journal
    "sun" -> R.string.icon_sun
    else -> R.string.icon_spa
}

fun formatMinutesOfDay(minutes: Int): String =
    LocalTime.of(minutes / 60, minutes % 60)
        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

fun formatDaysMask(mask: Int): String =
    DayOfWeek.entries
        .filter { mask and Habit.maskBit(it) != 0 }
        .joinToString(" · ") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }

// -------------------------------------------------------------- habit rows --

@Composable
fun HabitTodayRow(
    status: HabitWithStatus,
    onToggle: (Boolean) -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onOpen)
            .padding(vertical = 10.dp),
    ) {
        DoneToggle(done = status.doneToday, onToggle = onToggle)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = status.habit.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (status.doneToday) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            status.habit.reminderMinutesOfDay?.let { minutes ->
                val timeLabel = remember(minutes) { formatMinutesOfDay(minutes) }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        WeekDots(dots = status.weekDots)
    }
}

@Composable
private fun DoneToggle(done: Boolean, onToggle: (Boolean) -> Unit) {
    val actionLabel = stringResource(
        if (done) R.string.cd_habit_undone else R.string.cd_habit_done,
    )
    Surface(
        onClick = { onToggle(!done) },
        shape = CircleShape,
        color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (done) {
            null
        } else {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        },
        modifier = Modifier
            .size(36.dp)
            .semantics { contentDescription = actionLabel },
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (done) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null, // the Surface carries the action label
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun WeekDots(dots: List<HabitDayDot>) {
    val outline = MaterialTheme.colorScheme.outline
    val primary = MaterialTheme.colorScheme.primary
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        dots.forEach { dot ->
            val dotModifier = when (dot) {
                HabitDayDot.DONE -> Modifier.size(7.dp).background(primary, CircleShape)
                HabitDayDot.OPEN -> Modifier.size(7.dp).border(1.dp, outline.copy(alpha = 0.5f), CircleShape)
                HabitDayDot.OFF -> Modifier.size(7.dp)
            }
            Box(modifier = dotModifier)
        }
    }
}

@Composable
fun HabitOtherDayRow(
    habit: Habit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val daysLabel = remember(habit.daysMask) { formatDaysMask(habit.daysMask) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onOpen)
            .padding(vertical = 10.dp),
    ) {
        Icon(
            imageVector = habitIcon(habit.iconCode),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(text = habit.name, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(2.dp))
            Text(
                text = daysLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// --------------------------------------------------------------- week chart --

/**
 * Seven bars of completion fraction. Empty days keep a soft track so the
 * week's shape stays readable; nothing is red, a missed day is just shorter.
 */
@Composable
fun WeekBarChart(
    days: List<DayStat>,
    modifier: Modifier = Modifier,
) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val bar = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
        ) {
            if (days.isEmpty()) return@Canvas
            val slot = size.width / days.size
            val barWidth = slot * 0.42f
            val corner = CornerRadius(barWidth / 2f, barWidth / 2f)
            days.forEachIndexed { index, day ->
                val left = slot * index + (slot - barWidth) / 2f
                drawRoundRect(
                    color = track,
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = corner,
                    alpha = 0.4f,
                )
                val fraction = if (day.scheduled == 0) 0f else day.completed.toFloat() / day.scheduled
                if (fraction > 0f) {
                    val barHeight = size.height * fraction
                    drawRoundRect(
                        color = bar,
                        topLeft = Offset(left, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = corner,
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            days.forEach { day ->
                val initial = remember(day.epochDay) {
                    LocalDate.ofEpochDay(day.epochDay).dayOfWeek
                        .getDisplayName(TextStyle.NARROW, Locale.getDefault())
                }
                Text(
                    text = initial,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ----------------------------------------------------------------- insights --

private fun InsightCode.icon(): ImageVector = when (this) {
    InsightCode.PERFECT_WEEK -> Icons.Rounded.Star
    InsightCode.BEST_HABIT -> Icons.Rounded.ThumbUp
    InsightCode.FOCUS_HABIT -> Icons.Rounded.Flag
    InsightCode.CONSISTENCY_UP -> Icons.Rounded.TrendingUp
    InsightCode.CONSISTENCY_DOWN -> Icons.Rounded.Cached
    InsightCode.MOOD_TRENDING_UP -> Icons.Rounded.SentimentSatisfied
    InsightCode.URGES_EASING -> Icons.Rounded.Spa
    InsightCode.CHECKIN_STREAK -> Icons.Rounded.EventAvailable
    InsightCode.MILESTONE_NEAR -> Icons.Rounded.EmojiEvents
    InsightCode.GETTING_STARTED -> Icons.Rounded.AutoAwesome
}

@Composable
private fun insightText(insight: Insight): String = when (insight.code) {
    InsightCode.PERFECT_WEEK -> stringResource(R.string.insight_perfect_week)
    InsightCode.BEST_HABIT ->
        stringResource(R.string.insight_best_habit, insight.habitName.orEmpty(), insight.value ?: 0)
    InsightCode.FOCUS_HABIT ->
        stringResource(R.string.insight_focus_habit, insight.habitName.orEmpty())
    InsightCode.CONSISTENCY_UP ->
        stringResource(R.string.insight_consistency_up, insight.value ?: 0)
    InsightCode.CONSISTENCY_DOWN -> stringResource(R.string.insight_consistency_down)
    InsightCode.MOOD_TRENDING_UP -> stringResource(R.string.insight_mood_up)
    InsightCode.URGES_EASING -> stringResource(R.string.insight_urges_easing)
    InsightCode.CHECKIN_STREAK ->
        stringResource(R.string.insight_checkin_streak, insight.value ?: 0)
    InsightCode.MILESTONE_NEAR ->
        stringResource(R.string.insight_milestone_near, insight.value ?: 0)
    InsightCode.GETTING_STARTED -> stringResource(R.string.insight_getting_started)
}

@Composable
fun InsightRow(insight: Insight, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = insight.code.icon(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = insightText(insight),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

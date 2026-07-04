package com.runtimelabs.clarity.feature.home

import androidx.annotation.StringRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.SentimentNeutral
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.SentimentVeryDissatisfied
import androidx.compose.material.icons.rounded.SentimentVerySatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.ClarityCard
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.domain.model.DailyCheckIn
import com.runtimelabs.clarity.domain.model.MoodLevel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// ------------------------------------------------------------ mood mapping --

fun MoodLevel.icon(): ImageVector = when (this) {
    MoodLevel.STRUGGLING -> Icons.Rounded.SentimentVeryDissatisfied
    MoodLevel.LOW -> Icons.Rounded.SentimentDissatisfied
    MoodLevel.OKAY -> Icons.Rounded.SentimentNeutral
    MoodLevel.GOOD -> Icons.Rounded.SentimentSatisfied
    MoodLevel.GREAT -> Icons.Rounded.SentimentVerySatisfied
}

@StringRes
fun MoodLevel.labelRes(): Int = when (this) {
    MoodLevel.STRUGGLING -> R.string.mood_struggling
    MoodLevel.LOW -> R.string.mood_low
    MoodLevel.OKAY -> R.string.mood_okay
    MoodLevel.GOOD -> R.string.mood_good
    MoodLevel.GREAT -> R.string.mood_great
}

/**
 * A presence gradient, not a judgment scale: low moods are muted, high moods
 * saturated — never red for struggling. The week strip should read like
 * weather, not a report card.
 */
@Composable
fun MoodLevel.dotColor(): Color = when (this) {
    MoodLevel.STRUGGLING -> MaterialTheme.colorScheme.outline
    MoodLevel.LOW -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    MoodLevel.OKAY -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    MoodLevel.GOOD -> MaterialTheme.colorScheme.primary.copy(alpha = 0.80f)
    MoodLevel.GREAT -> MaterialTheme.colorScheme.primary
}

// -------------------------------------------------------------- streak ring --

/**
 * The dashboard hero: days clean inside a milestone-progress ring. Progress
 * caps at 100% — past the milestone the ring stays full and the caption
 * switches to "keep going" (handled by the caller). Entry sweep respects
 * reduce-motion via [snap].
 */
@Composable
fun StreakRing(
    currentDays: Int,
    milestoneDays: Int,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val target = if (milestoneDays <= 0) 0f else (currentDays / milestoneDays.toFloat()).coerceIn(0f, 1f)
    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = if (reduceMotion) snap() else tween(900, easing = FastOutSlowInEasing),
        label = "streakRingProgress",
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val barColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier.size(210.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 14.dp.toPx()
            val radius = (size.minDimension - strokePx) / 2f
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokePx),
                alpha = 0.45f,
            )
            if (progress > 0f) {
                drawArc(
                    color = barColor,
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentDays.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = pluralStringResource(R.plurals.streak_days_label, currentDays),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ------------------------------------------------------------ check-in card --

@Composable
fun CheckInCard(
    todayCheckIn: DailyCheckIn?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ClarityCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (todayCheckIn == null) {
                    Text(
                        text = stringResource(R.string.checkin_card_title_todo),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.checkin_card_subtitle_todo),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.checkin_card_title_done),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(todayCheckIn.mood.labelRes()) +
                            " · " + stringResource(R.string.checkin_card_edit),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = todayCheckIn?.mood?.icon() ?: Icons.Rounded.ChevronRight,
                contentDescription = null, // card text carries the semantics
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

// -------------------------------------------------------------- week strip --

@Composable
fun MoodWeekCard(
    weekCheckIns: List<DailyCheckIn>,
    todayEpochDay: Long,
    modifier: Modifier = Modifier,
) {
    ClarityCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.home_week_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            (6 downTo 0).forEach { offset ->
                val day = todayEpochDay - offset
                val checkIn = weekCheckIns.firstOrNull { it.epochDay == day }
                DayDot(epochDay = day, mood = checkIn?.mood)
            }
        }
    }
}

@Composable
private fun DayDot(epochDay: Long, mood: MoodLevel?) {
    val dayInitial = remember(epochDay) {
        LocalDate.ofEpochDay(epochDay).dayOfWeek
            .getDisplayName(TextStyle.NARROW, Locale.getDefault())
    }
    val outline = MaterialTheme.colorScheme.outline
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val dotModifier = if (mood != null) {
            Modifier.size(16.dp).background(mood.dotColor(), CircleShape)
        } else {
            Modifier.size(16.dp).border(1.dp, outline.copy(alpha = 0.4f), CircleShape)
        }
        Box(modifier = dotModifier)
        Spacer(Modifier.height(6.dp))
        Text(
            text = dayInitial,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// -------------------------------------------------------------- daily quote --

/**
 * Deterministic: epochDay modulo the (all-original) quote list. Everyone sees
 * the same line all day; it changes at local midnight; nothing is stored.
 */
@Composable
fun QuoteCard(todayEpochDay: Long, modifier: Modifier = Modifier) {
    val quotes = stringArrayResource(R.array.daily_quotes)
    if (quotes.isEmpty()) return
    val quote = quotes[(todayEpochDay % quotes.size).toInt()]

    ClarityCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.home_quote_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text(
            text = quote,
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

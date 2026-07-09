package com.runtimelabs.clarity.feature.achievement

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.ClarityPrimaryButton
import com.runtimelabs.clarity.core.designsystem.components.ConfettiOverlay
import com.runtimelabs.clarity.core.designsystem.theme.extended
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.domain.badge.Badge

/**
 * The unlock-animation + confetti moment. Shown as a full-screen [Dialog]
 * (not [androidx.compose.material3.AlertDialog] — this needs full custom
 * layout and to sit above the bottom nav / any screen content) whenever one
 * or more badges were genuinely earned just now — never for badges that
 * turn out to already be unlocked (see
 * [com.runtimelabs.clarity.domain.repository.BadgeRepository.evaluateAndUnlock]'s
 * "genuinely new" contract). See [ConfettiOverlay]'s doc comment for why a
 * one-shot celebration here doesn't contradict this app's general
 * not-gamified stance elsewhere.
 *
 * [newlyUnlocked] can hold more than one badge (e.g. reaching Day 7 the
 * same day a fifth journal entry is written). Shown one at a time with a
 * small "1 of 2" progress label rather than cramming several badges into
 * one card — each one earned its own moment.
 */
@Composable
fun AchievementUnlockOverlay(
    newlyUnlocked: List<Badge>,
    reduceMotion: Boolean,
    onDismiss: () -> Unit,
) {
    if (newlyUnlocked.isEmpty()) return

    var index by remember(newlyUnlocked) { mutableIntStateOf(0) }
    val badge = newlyUnlocked.getOrNull(index)
    if (badge == null) {
        onDismiss()
        return
    }
    val isLast = index == newlyUnlocked.lastIndex

    Dialog(
        onDismissRequest = { /* deliberately not dismissible by scrim tap — a real moment, not an accidental one */ },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center,
        ) {
            ConfettiOverlay(play = true, reduceMotion = reduceMotion, modifier = Modifier.fillMaxSize())

            UnlockCard(
                badge = badge,
                reduceMotion = reduceMotion,
                position = index + 1,
                total = newlyUnlocked.size,
                isLast = isLast,
                onContinue = {
                    if (isLast) onDismiss() else index += 1
                },
            )
        }
    }
}

@Composable
private fun UnlockCard(
    badge: Badge,
    reduceMotion: Boolean,
    position: Int,
    total: Int,
    isLast: Boolean,
    onContinue: () -> Unit,
) {
    // Keyed on the badge itself so the pop-in replays for each badge in a
    // multi-badge sequence, not just the first.
    val entrance = remember(badge) { Animatable(0f) }
    LaunchedEffect(badge) {
        entrance.snapTo(0f)
        if (reduceMotion) {
            entrance.snapTo(1f)
        } else {
            entrance.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
    }
    val entranceValue = entrance.value
    val scale = 0.7f + entranceValue * 0.3f

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier
            .padding(MaterialTheme.spacing.lg)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = entranceValue
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(MaterialTheme.spacing.xl)
                .fillMaxWidth(),
        ) {
            if (total > 1) {
                Text(
                    text = stringResource(R.string.achievement_unlock_progress, position, total),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
            }
            Text(
                text = stringResource(R.string.achievement_unlock_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.extended.celebration,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.md))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.dp)
                    .background(MaterialTheme.extended.celebration.copy(alpha = 0.18f), CircleShape),
            ) {
                Icon(
                    imageVector = badge.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.extended.celebration,
                    modifier = Modifier.size(44.dp),
                )
            }
            Spacer(Modifier.height(MaterialTheme.spacing.lg))
            Text(
                text = stringResource(badge.titleRes()),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.xs))
            Text(
                text = stringResource(badge.descriptionRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.lg))
            Text(
                text = stringResource(badge.unlockQuoteRes()),
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.xl))
            ClarityPrimaryButton(
                text = stringResource(
                    if (isLast) R.string.achievement_unlock_continue else R.string.achievement_unlock_next,
                ),
                onClick = onContinue,
            )
        }
    }
}

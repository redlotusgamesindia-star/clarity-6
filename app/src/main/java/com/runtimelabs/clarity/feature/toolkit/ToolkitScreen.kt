package com.runtimelabs.clarity.feature.toolkit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.BreathingIndicator
import com.runtimelabs.clarity.core.designsystem.components.ClarityCard
import com.runtimelabs.clarity.core.designsystem.components.ClarityPrimaryButton
import com.runtimelabs.clarity.core.designsystem.theme.spacing

/**
 * The SOS destination — the dawn-amber button finally has its purpose.
 * Breathing is the hero (one tap, auto-starts); everything else is a card
 * away. Pushed on top of whatever tab the user was in, so leaving here
 * returns them exactly where they were.
 */
@Composable
fun ToolkitScreen(
    onBack: () -> Unit,
    onBreathe: () -> Unit,
    onGrounding: () -> Unit,
    onMuscle: () -> Unit,
    onReframe: () -> Unit,
    onWhy: () -> Unit,
) {
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

                Spacer(Modifier.height(MaterialTheme.spacing.lg))
                ToolCard(
                    icon = Icons.Rounded.Visibility,
                    titleRes = R.string.toolkit_grounding_title,
                    subtitleRes = R.string.toolkit_grounding_subtitle,
                    onClick = onGrounding,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                ToolCard(
                    icon = Icons.Rounded.SelfImprovement,
                    titleRes = R.string.toolkit_muscle_title,
                    subtitleRes = R.string.toolkit_muscle_subtitle,
                    onClick = onMuscle,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                ToolCard(
                    icon = Icons.Rounded.Psychology,
                    titleRes = R.string.toolkit_reframe_title,
                    subtitleRes = R.string.toolkit_reframe_subtitle,
                    onClick = onReframe,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                ToolCard(
                    icon = Icons.Rounded.Favorite,
                    titleRes = R.string.toolkit_why_title,
                    subtitleRes = R.string.toolkit_why_subtitle,
                    onClick = onWhy,
                )

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
                Spacer(Modifier.height(2.dp))
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

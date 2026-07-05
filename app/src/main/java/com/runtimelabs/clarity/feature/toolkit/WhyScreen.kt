package com.runtimelabs.clarity.feature.toolkit

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.runtimelabs.clarity.core.designsystem.components.BreathingIndicator
import com.runtimelabs.clarity.core.designsystem.components.ClarityCard
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.feature.onboarding.labelRes
import com.runtimelabs.clarity.feature.onboarding.supportingRes

@Composable
fun WhyScreen(
    onDone: () -> Unit,
    viewModel: WhyViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = MaterialTheme.spacing.sm),
            ) {
                IconButton(onClick = onDone) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            }

            when (val current = state) {
                WhyUiState.Loading -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    BreathingIndicator(size = 48.dp)
                }
                WhyUiState.Empty -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = MaterialTheme.spacing.xl),
                ) {
                    Text(
                        text = stringResource(R.string.why_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                is WhyUiState.Ready -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
                ) {
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                    Text(
                        text = stringResource(R.string.why_header),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.lg))
                    current.reasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = MaterialTheme.spacing.sm),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary, // the dawn accent
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = stringResource(reason.labelRes()),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                    Spacer(Modifier.height(MaterialTheme.spacing.lg))
                    ClarityCard {
                        Text(
                            text = stringResource(R.string.why_goal_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(MaterialTheme.spacing.xs))
                        Text(
                            text = stringResource(current.goal.labelRes()),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(current.goal.supportingRes()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(MaterialTheme.spacing.xxl))
                }
            }
        }
    }
}

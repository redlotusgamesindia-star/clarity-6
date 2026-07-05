package com.runtimelabs.clarity.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.WorkspacePremium
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
import com.runtimelabs.clarity.ads.ClarityBannerAd
import com.runtimelabs.clarity.core.designsystem.components.ClarityCard
import com.runtimelabs.clarity.core.designsystem.components.ClarityPrimaryButton
import com.runtimelabs.clarity.core.designsystem.components.ClarityTextButton
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.domain.ads.AdScreen
import com.runtimelabs.clarity.domain.premium.PremiumState

/**
 * Minimal by design — this exists to host the Premium section requested
 * here, not to become a general settings hub in this pass. Theme switching
 * ([com.runtimelabs.clarity.domain.repository.SettingsRepository.themeMode]
 * already exists with no UI anywhere) is a natural, low-risk follow-up
 * this screen could grow into later, deliberately left out of this scope.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val premiumState by viewModel.premiumState.collectAsStateWithLifecycle()

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
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = MaterialTheme.spacing.xs),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
            ) {
                Spacer(Modifier.height(MaterialTheme.spacing.md))
                Text(
                    text = stringResource(R.string.settings_premium_section_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                PremiumSection(premiumState = premiumState)

                Spacer(Modifier.height(MaterialTheme.spacing.xl))
                ClarityBannerAd(screen = AdScreen.SETTINGS)

                Spacer(Modifier.height(MaterialTheme.spacing.xxl))
            }
        }
    }
}

@Composable
private fun PremiumSection(premiumState: PremiumState) {
    ClarityCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.WorkspacePremium,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = MaterialTheme.spacing.md),
            )
            Column {
                Text(
                    text = stringResource(R.string.settings_current_status_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        when (premiumState) {
                            is PremiumState.Free -> R.string.settings_status_free
                            is PremiumState.Premium -> R.string.settings_status_premium
                        },
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        Text(
            text = stringResource(R.string.settings_remove_ads_title),
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.settings_coming_soon),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        ClarityPrimaryButton(
            text = stringResource(R.string.settings_remove_ads_button),
            onClick = {},
            enabled = false,
        )

        Spacer(Modifier.height(MaterialTheme.spacing.md))
        ClarityTextButton(
            text = stringResource(R.string.settings_restore_purchases_button),
            onClick = {},
            enabled = false,
        )
    }
}

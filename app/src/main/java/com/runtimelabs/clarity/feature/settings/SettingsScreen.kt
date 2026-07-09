package com.runtimelabs.clarity.feature.settings

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.ads.ClarityBannerAd
import com.runtimelabs.clarity.core.designsystem.components.ClarityCard
import com.runtimelabs.clarity.core.designsystem.components.ClarityPrimaryButton
import com.runtimelabs.clarity.core.designsystem.components.ClarityTextButton
import com.runtimelabs.clarity.core.designsystem.theme.MotionTokens
import com.runtimelabs.clarity.core.designsystem.theme.extended
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.domain.ads.AdScreen
import com.runtimelabs.clarity.domain.premium.PremiumState
import com.runtimelabs.clarity.domain.premium.PurchaseResult

/**
 * Minimal by design — this exists to host the Premium section, not to
 * become a general settings hub. Theme switching
 * ([com.runtimelabs.clarity.domain.repository.SettingsRepository.themeMode]
 * already exists with no UI anywhere) is a natural, low-risk follow-up
 * this screen could grow into later, deliberately left out of this scope.
 *
 * Every purchase-result outcome from [SettingsViewModel] gets exactly one
 * piece of feedback, shown once, then consumed: [PurchaseResult.Success]
 * is carried entirely by the badge's own state-driven crossfade (no
 * redundant snackbar — the badge appearing IS the celebration).
 * [PurchaseResult.Cancelled] gets nothing at all; cancelling a purchase is
 * an ordinary choice, not a failure worth interrupting anyone over. Every
 * other outcome gets a brief, calm [Snackbar].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val pendingMessage = stringResource(R.string.purchase_result_pending_message)
    val unavailableMessage = stringResource(R.string.purchase_result_unavailable_message)
    val nothingToRestoreMessage = stringResource(R.string.purchase_result_nothing_to_restore_message)

    LaunchedEffect(state.purchaseResult) {
        val result = state.purchaseResult ?: return@LaunchedEffect
        val message = when (result) {
            PurchaseResult.Success, PurchaseResult.Cancelled -> null
            PurchaseResult.Pending -> pendingMessage
            PurchaseResult.BillingUnavailable -> unavailableMessage
            PurchaseResult.NothingToRestore -> nothingToRestoreMessage
            is PurchaseResult.Error -> result.message
        }
        if (message != null) {
            snackbarHostState.showSnackbar(message)
        }
        viewModel.onPurchaseResultConsumed()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                PremiumSection(
                    state = state,
                    onRemoveAdsClicked = { (context as? Activity)?.let(viewModel::onRemoveAdsClicked) },
                    onRestoreClicked = viewModel::onRestorePurchasesClicked,
                )

                Spacer(Modifier.height(MaterialTheme.spacing.xl))
                ClarityBannerAd(screen = AdScreen.SETTINGS)

                Spacer(Modifier.height(MaterialTheme.spacing.xxl))
            }
        }
    }
}

@Composable
private fun PremiumSection(
    state: SettingsUiState,
    onRemoveAdsClicked: () -> Unit,
    onRestoreClicked: () -> Unit,
) {
    ClarityCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_current_status_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.hairline))
                Text(
                    text = stringResource(
                        when (state.premiumState) {
                            PremiumState.Free -> R.string.settings_remove_ads_description
                            PremiumState.Premium -> R.string.settings_premium_active_description
                            PremiumState.Pending -> R.string.settings_pending_description
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(MaterialTheme.spacing.md))
            // The state-driven crossfade here IS the "smooth success
            // animation" requirement — Free's plain state fades out and the
            // Premium badge fades and scales in the instant purchaseState
            // flips, with zero extra wiring beyond what PremiumManager's
            // reactive chain already provides.
            AnimatedContent(
                targetState = state.premiumState,
                transitionSpec = {
                    (fadeIn(tween(MotionTokens.EMPHASIZED)) togetherWith fadeOut(tween(MotionTokens.QUICK)))
                },
                label = "premiumStatusBadge",
            ) { premiumState ->
                StatusBadge(premiumState)
            }
        }

        if (state.premiumState == PremiumState.Free) {
            Spacer(Modifier.height(MaterialTheme.spacing.lg))
            ClarityPrimaryButton(
                text = stringResource(R.string.settings_remove_ads_button),
                onClick = onRemoveAdsClicked,
                enabled = !state.isBusy,
                loading = state.isPurchasing,
            )
        }

        Spacer(Modifier.height(MaterialTheme.spacing.md))
        ClarityTextButton(
            text = stringResource(R.string.settings_restore_purchases_button),
            onClick = onRestoreClicked,
            enabled = !state.isBusy,
        )
    }
}

@Composable
private fun StatusBadge(premiumState: PremiumState) {
    val (icon, labelRes, tint, background) = when (premiumState) {
        PremiumState.Free -> BadgeStyle(
            icon = null,
            labelRes = R.string.settings_status_free,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            background = MaterialTheme.colorScheme.surfaceVariant,
        )
        PremiumState.Premium -> BadgeStyle(
            icon = Icons.Rounded.WorkspacePremium,
            labelRes = R.string.settings_status_premium,
            tint = MaterialTheme.extended.celebration,
            background = MaterialTheme.extended.celebration.copy(alpha = 0.15f),
        )
        PremiumState.Pending -> BadgeStyle(
            icon = Icons.Rounded.HourglassTop,
            labelRes = R.string.settings_status_pending,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            background = MaterialTheme.colorScheme.surfaceVariant,
        )
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = background,
        modifier = Modifier.wrapContentWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelLarge,
                color = tint,
            )
        }
    }
}

private data class BadgeStyle(
    val icon: ImageVector?,
    val labelRes: Int,
    val tint: Color,
    val background: Color,
)

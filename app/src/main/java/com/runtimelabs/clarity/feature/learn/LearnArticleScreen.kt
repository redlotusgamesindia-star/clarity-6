package com.runtimelabs.clarity.feature.learn

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.theme.extended
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.domain.learn.LearnArticle

/**
 * The reader. Same top-bar shape as [com.runtimelabs.clarity.feature.settings.SettingsScreen]
 * and [com.runtimelabs.clarity.feature.achievement.AchievementsScreen] (56dp
 * row, back icon, title) — no ViewModel, `article` arrives fully resolved
 * from the nav graph since there's nothing to load.
 */
@Composable
fun LearnArticleScreen(article: LearnArticle, onBack: () -> Unit) {
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.extended.celebration.copy(alpha = 0.14f), CircleShape),
            ) {
                Icon(
                    imageVector = article.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.extended.celebration,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.height(MaterialTheme.spacing.md))
            Text(
                text = stringResource(article.titleRes()),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.xs))
            Text(
                text = stringResource(R.string.learn_read_minutes, article.readMinutes()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.lg))
            Text(
                text = stringResource(article.bodyRes()),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.xxl))
        }
    }
}

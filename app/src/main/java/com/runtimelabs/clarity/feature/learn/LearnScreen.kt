package com.runtimelabs.clarity.feature.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.components.ClarityCard
import com.runtimelabs.clarity.core.designsystem.theme.extended
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.domain.learn.LearnArticle
import com.runtimelabs.clarity.domain.learn.LearnCategory

/**
 * The educational library. Static, bundled content grouped into three
 * short categories — no ViewModel needed, [LearnArticle] is a closed enum
 * with nothing to load or observe, same reasoning [com.runtimelabs.clarity.feature.toolkit.ToolkitScreen]'s
 * own hub already applies to its list of tools. Deliberately NOT on the ad
 * allow-list (see [com.runtimelabs.clarity.domain.ads.AdScreen] and the
 * manifest's own privacy-posture comment, which names exactly three
 * ad-eligible screens) — this sits next to the Journal and SOS toolkit in
 * spirit, both already ad-free, not next to Home and Journey.
 */
@Composable
fun LearnScreen(onOpenArticle: (LearnArticle) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = MaterialTheme.spacing.screenHorizontal,
            end = MaterialTheme.spacing.screenHorizontal,
            top = MaterialTheme.spacing.md,
            bottom = MaterialTheme.spacing.xxl,
        ),
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.nav_learn),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.xs))
                Text(
                    text = stringResource(R.string.learn_screen_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.lg))
            }
        }
        LearnCategory.ORDERED.forEach { category ->
            item(key = "header:${category.name}") {
                CategoryHeader(category = category)
            }
            items(LearnArticle.inCategory(category), key = { it.name }) { article ->
                ArticleRow(
                    article = article,
                    onClick = { onOpenArticle(article) },
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm),
                )
            }
            item(key = "spacer:${category.name}") {
                Spacer(Modifier.height(MaterialTheme.spacing.md))
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: LearnCategory) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm),
    ) {
        Icon(
            imageVector = category.icon(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(MaterialTheme.spacing.xs))
        Text(
            text = stringResource(category.titleRes()),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ArticleRow(article: LearnArticle, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ClarityCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.extended.celebration.copy(alpha = 0.14f), CircleShape),
            ) {
                Icon(
                    imageVector = article.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.extended.celebration,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(MaterialTheme.spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(article.titleRes()),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.hairline))
                Text(
                    text = stringResource(article.summaryRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.xs))
                Text(
                    text = stringResource(R.string.learn_read_minutes, article.readMinutes()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

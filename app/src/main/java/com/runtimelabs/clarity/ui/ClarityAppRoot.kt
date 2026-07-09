package com.runtimelabs.clarity.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.runtimelabs.clarity.R
import com.runtimelabs.clarity.core.designsystem.theme.spacing
import com.runtimelabs.clarity.feature.onboarding.OnboardingScreen
import com.runtimelabs.clarity.navigation.ClarityNavHost
import com.runtimelabs.clarity.navigation.SosRoute
import com.runtimelabs.clarity.navigation.TopLevelDestination
import com.runtimelabs.clarity.navigation.isRouteInHierarchy

private val SosButtonSize = 64.dp
private val SosOverhang = 32.dp        // how far the SOS button rises above the bar
private val BarContentHeight = 64.dp

/**
 * App shell. Decides between onboarding and the main scaffold reactively:
 * when onboarding flips the persisted flag, this recomposes straight into
 * the main experience — no manual navigation call, no stale state.
 */
@Composable
fun ClarityAppRoot(onboardingCompleted: Boolean) {
    if (!onboardingCompleted) {
        OnboardingScreen()
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Bar is visible only on the four tabs. Full-screen flows (SOS now;
    // relapse reset, celebrations later) hide it so nothing competes for
    // attention. `null` (first frame) counts as top-level to avoid flicker.
    val isTopLevel = currentDestination == null ||
        TopLevelDestination.entries.any { currentDestination.isRouteInHierarchy(it.routeClass) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevel) {
                ClarityBottomBar(
                    destinations = TopLevelDestination.entries,
                    currentDestination = currentDestination,
                    onNavigateToDestination = navController::navigateToTopLevel,
                    onSosClick = { navController.navigate(SosRoute) },
                )
            }
        },
    ) { padding ->
        ClarityNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
        )
    }
}

/**
 * Standard top-level tab navigation: single instance per tab, state saved
 * and restored, back always returns to the start tab.
 */
private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    val options = navOptions {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
    navigate(destination.route, options)
}

/**
 * Custom bottom bar: four tabs split around a raised SOS button.
 *
 * Not Material3's NavigationBar — the center action can't be expressed there
 * without a dummy disabled item. The whole component (bar + protruding
 * button) lives in one Box tall enough to contain the button, so the full
 * 64dp circle stays hit-testable. Scaffold measures the Box and pads content
 * accordingly.
 */
@Composable
private fun ClarityBottomBar(
    destinations: List<TopLevelDestination>,
    currentDestination: NavDestination?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    onSosClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier
                .padding(top = SosOverhang)
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(BarContentHeight)
                    .selectableGroup(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.take(2).forEach { destination ->
                    ClarityNavItem(
                        destination = destination,
                        selected = currentDestination.isRouteInHierarchy(destination.routeClass),
                        onClick = { onNavigateToDestination(destination) },
                    )
                }
                Spacer(Modifier.weight(1f)) // clearance under the SOS button
                destinations.drop(2).forEach { destination ->
                    ClarityNavItem(
                        destination = destination,
                        selected = currentDestination.isRouteInHierarchy(destination.routeClass),
                        onClick = { onNavigateToDestination(destination) },
                    )
                }
            }
        }
        SosButton(
            onClick = onSosClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun RowScope.ClarityNavItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 200),
        label = "navItemColor",
    )
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = null, // label below carries the semantics
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(MaterialTheme.spacing.hairline))
        Text(
            text = stringResource(destination.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

/**
 * The one loud element in the app: dawn-amber circle, always one tap away.
 * tertiary/onTertiary keep contrast correct in both themes.
 */
@Composable
private fun SosButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
        shadowElevation = 6.dp,
        modifier = modifier.size(SosButtonSize),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.Rounded.Spa,
                contentDescription = stringResource(R.string.sos_button),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

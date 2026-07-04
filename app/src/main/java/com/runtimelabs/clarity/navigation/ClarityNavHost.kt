package com.runtimelabs.clarity.navigation

import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.runtimelabs.clarity.core.designsystem.components.PlaceholderScreen
import kotlin.reflect.KClass

/**
 * The single navigation graph. Every destination currently renders a
 * clearly-marked [PlaceholderScreen]; each is replaced in its feature phase
 * (Home/SOS: Phase A, Journey/Journal: Phase B, Learn: Phase C) without any
 * change to this graph's structure.
 */
@Composable
fun ClarityNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            PlaceholderScreen(
                title = "Home",
                plannedPhase = "Dashboard — streak ring, check-in, daily quote. Ships in Phase A.",
            )
        }
        composable<JourneyRoute> {
            PlaceholderScreen(
                title = "Journey",
                plannedPhase = "Timeline, analytics, achievements. Ships in Phase B.",
            )
        }
        composable<LearnRoute> {
            PlaceholderScreen(
                title = "Learn",
                plannedPhase = "Educational library and daily tips. Ships in Phase C.",
            )
        }
        composable<JournalRoute> {
            PlaceholderScreen(
                title = "Journal",
                plannedPhase = "Private journal with tags and search. Ships in Phase B.",
            )
        }
        composable<SosRoute> {
            PlaceholderScreen(
                title = "Urge toolkit",
                plannedPhase = "Breathing, delay timer, grounding, redirection. Ships in Phase A.",
                onBack = { navController.popBackStack() },
            )
        }
    }
}

/** NiA-style hierarchy check for bottom-bar selected state. */
fun NavDestination?.isRouteInHierarchy(route: KClass<*>): Boolean =
    this?.hierarchy?.any { it.hasRoute(route) } == true

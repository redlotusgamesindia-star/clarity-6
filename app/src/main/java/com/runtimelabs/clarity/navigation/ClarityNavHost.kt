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
import com.runtimelabs.clarity.feature.home.HomeScreen
import com.runtimelabs.clarity.feature.journal.JournalEditorScreen
import com.runtimelabs.clarity.feature.journal.JournalListScreen
import com.runtimelabs.clarity.feature.journey.HabitEditorScreen
import com.runtimelabs.clarity.feature.journey.JourneyScreen
import kotlin.reflect.KClass

/**
 * The single navigation graph. Home and Journal are live; the remaining
 * destinations render a clearly-marked [PlaceholderScreen] until their
 * phase (SOS: Phase A, Journey: Phase B, Learn: Phase C).
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
            HomeScreen()
        }
        composable<JourneyRoute> {
            JourneyScreen(
                onNewHabit = { navController.navigate(HabitEditorRoute()) },
                onEditHabit = { id -> navController.navigate(HabitEditorRoute(habitId = id)) },
            )
        }
        composable<HabitEditorRoute> {
            HabitEditorScreen(onDone = { navController.popBackStack() })
        }
        composable<LearnRoute> {
            PlaceholderScreen(
                title = "Learn",
                plannedPhase = "Educational library and daily tips. Ships in Phase C.",
            )
        }
        composable<JournalRoute> {
            JournalListScreen(
                onOpenEntry = { id -> navController.navigate(JournalEditorRoute(entryId = id)) },
                onNewEntry = { navController.navigate(JournalEditorRoute()) },
            )
        }
        composable<JournalEditorRoute> {
            JournalEditorScreen(onDone = { navController.popBackStack() })
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

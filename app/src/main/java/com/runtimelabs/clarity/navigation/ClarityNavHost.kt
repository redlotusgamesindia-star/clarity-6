package com.runtimelabs.clarity.navigation

import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.runtimelabs.clarity.core.designsystem.components.PlaceholderScreen
import com.runtimelabs.clarity.feature.home.HomeScreen
import com.runtimelabs.clarity.feature.journal.GratitudeScreen
import com.runtimelabs.clarity.feature.journal.JournalEditorScreen
import com.runtimelabs.clarity.feature.journal.JournalEntryKind
import com.runtimelabs.clarity.feature.journal.JournalListScreen
import com.runtimelabs.clarity.feature.journal.ThoughtRecordScreen
import com.runtimelabs.clarity.feature.journey.HabitEditorScreen
import com.runtimelabs.clarity.feature.journey.JourneyScreen
import com.runtimelabs.clarity.feature.recovery.RecoveryFlowScreen
import com.runtimelabs.clarity.feature.toolkit.BreathingScreen
import com.runtimelabs.clarity.feature.toolkit.EXERCISE_GROUNDING as EXERCISE_GROUNDING_CODE
import com.runtimelabs.clarity.feature.toolkit.EXERCISE_MUSCLE as EXERCISE_MUSCLE_CODE
import com.runtimelabs.clarity.feature.toolkit.GuidedStepsScreen
import com.runtimelabs.clarity.feature.toolkit.ToolkitScreen
import com.runtimelabs.clarity.feature.toolkit.WhyScreen
import kotlin.reflect.KClass

/**
 * The single navigation graph. Home, Journey, Journal, and the SOS toolkit
 * subgraph are all live; only Learn remains a [PlaceholderScreen]
 * (Phase C, educational library).
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
            HomeScreen(
                onNavigateToRecoveryFlow = { eventId ->
                    navController.navigate(RelapseRecoveryRoute(relapseJourneyEventId = eventId))
                },
            )
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
                onOpenEntry = { entry ->
                    val route = when (entry.kind) {
                        JournalEntryKind.FREE -> JournalEditorRoute(entryId = entry.id)
                        JournalEntryKind.THOUGHT -> ThoughtRecordEditorRoute(recordId = entry.id)
                        JournalEntryKind.GRATITUDE -> GratitudeEditorRoute(entryId = entry.id)
                    }
                    navController.navigate(route)
                },
                onNewEntry = { kind ->
                    val route = when (kind) {
                        JournalEntryKind.FREE -> JournalEditorRoute()
                        JournalEntryKind.THOUGHT -> ThoughtRecordEditorRoute()
                        JournalEntryKind.GRATITUDE -> GratitudeEditorRoute()
                    }
                    navController.navigate(route)
                },
            )
        }
        composable<JournalEditorRoute> {
            JournalEditorScreen(onDone = { navController.popBackStack() })
        }
        composable<ThoughtRecordEditorRoute> {
            ThoughtRecordScreen(onDone = { navController.popBackStack() })
        }
        composable<GratitudeEditorRoute> {
            GratitudeScreen(onDone = { navController.popBackStack() })
        }
        composable<SosRoute> {
            ToolkitScreen(
                onBack = { navController.popBackStack() },
                onBreathe = { navController.navigate(BreathingRoute) },
                onGrounding = {
                    navController.navigate(GuidedStepsRoute(EXERCISE_GROUNDING_CODE))
                },
                onMuscle = {
                    navController.navigate(GuidedStepsRoute(EXERCISE_MUSCLE_CODE))
                },
                onReframe = { navController.navigate(ThoughtRecordEditorRoute()) },
                onWhy = { navController.navigate(WhyRoute) },
            )
        }
        composable<BreathingRoute> {
            BreathingScreen(onDone = { navController.popBackStack() })
        }
        composable<GuidedStepsRoute> { entry ->
            val route: GuidedStepsRoute = entry.toRoute()
            GuidedStepsScreen(
                exerciseCode = route.exerciseCode,
                onDone = { navController.popBackStack() },
            )
        }
        composable<WhyRoute> {
            WhyScreen(onDone = { navController.popBackStack() })
        }
        composable<RelapseRecoveryRoute> { entry ->
            val route: RelapseRecoveryRoute = entry.toRoute()
            RecoveryFlowScreen(
                relapseJourneyEventId = route.relapseJourneyEventId,
                onDone = {
                    // Pop all the way back to Home, not just one step — the
                    // flow's own back button already lets someone revisit
                    // earlier steps, but "done" should never leave the
                    // five-step flow sitting on the back stack to return to.
                    navController.popBackStack(HomeRoute, inclusive = false)
                },
                onOpenBreathing = { navController.navigate(BreathingRoute) },
                onOpenJournal = { navController.navigate(JournalEditorRoute()) },
            )
        }
    }
}

/** NiA-style hierarchy check for bottom-bar selected state. */
fun NavDestination?.isRouteInHierarchy(route: KClass<*>): Boolean =
    this?.hierarchy?.any { it.hasRoute(route) } == true

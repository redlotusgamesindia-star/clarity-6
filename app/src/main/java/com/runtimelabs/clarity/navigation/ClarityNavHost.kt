package com.runtimelabs.clarity.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIntoContainer
import androidx.compose.animation.slideOutOfContainer
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.runtimelabs.clarity.core.designsystem.theme.MotionTokens
import com.runtimelabs.clarity.core.util.rememberReduceMotionEnabled
import com.runtimelabs.clarity.domain.learn.LearnArticle
import com.runtimelabs.clarity.domain.toolkit.ToolkitTool
import com.runtimelabs.clarity.feature.achievement.AchievementsScreen
import com.runtimelabs.clarity.feature.home.HomeScreen
import com.runtimelabs.clarity.feature.journal.GratitudeScreen
import com.runtimelabs.clarity.feature.journal.JournalEditorScreen
import com.runtimelabs.clarity.feature.journal.JournalEntryKind
import com.runtimelabs.clarity.feature.journal.JournalListScreen
import com.runtimelabs.clarity.feature.journal.ThoughtRecordScreen
import com.runtimelabs.clarity.feature.journey.HabitEditorScreen
import com.runtimelabs.clarity.feature.journey.JourneyScreen
import com.runtimelabs.clarity.feature.learn.LearnArticleScreen
import com.runtimelabs.clarity.feature.learn.LearnScreen
import com.runtimelabs.clarity.feature.recovery.RecoveryFlowScreen
import com.runtimelabs.clarity.feature.settings.SettingsScreen
import com.runtimelabs.clarity.feature.toolkit.BreathingScreen
import com.runtimelabs.clarity.feature.toolkit.EXERCISE_DISTRACTION as EXERCISE_DISTRACTION_CODE
import com.runtimelabs.clarity.feature.toolkit.EXERCISE_GROUNDING as EXERCISE_GROUNDING_CODE
import com.runtimelabs.clarity.feature.toolkit.EXERCISE_MUSCLE as EXERCISE_MUSCLE_CODE
import com.runtimelabs.clarity.feature.toolkit.GuidedStepsScreen
import com.runtimelabs.clarity.feature.toolkit.ReminderToolScreen
import com.runtimelabs.clarity.feature.toolkit.ToolkitScreen
import com.runtimelabs.clarity.feature.toolkit.WalkTimerScreen
import com.runtimelabs.clarity.feature.toolkit.WhyScreen
import kotlin.reflect.KClass

/**
 * The single navigation graph. Home, Journey, Journal, Learn, and the SOS
 * toolkit subgraph are all live.
 *
 * Transitions (polish pass): tab-to-tab switches crossfade only — no
 * directional slide, matching the conventional bottom-nav feel and keeping
 * a very-high-frequency action visually quiet, the same restraint already
 * applied to the streak ring and Comeback Achievements (ARCHITECTURE.md
 * §21/§22: motion that's seen constantly must stay calm, not busy).
 * Everything else (pushing a detail screen, popping back) gets a
 * direction-aware slide + fade using [AnimatedContentTransitionScope]'s
 * Start/End semantics rather than Left/Right, so it's correct under RTL
 * layouts too (the manifest already declares `supportsRtl="true"`).
 * [rememberReduceMotionEnabled] is read once here (composable context) and
 * captured by the plain transition lambdas below, which aren't themselves
 * `@Composable` — reduce-motion collapses every transition to a plain
 * crossfade, same degrade-to-static contract every other animation in this
 * app already follows.
 */
@Composable
fun ClarityNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = rememberReduceMotionEnabled()

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        enterTransition = {
            when {
                reduceMotion -> fadeIn(tween(MotionTokens.QUICK))
                isTopLevelTransition(initialState, targetState) -> fadeIn(tween(MotionTokens.STANDARD))
                else -> fadeIn(tween(MotionTokens.STANDARD, easing = MotionTokens.Standard)) +
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(MotionTokens.STANDARD, easing = MotionTokens.Standard),
                    )
            }
        },
        exitTransition = {
            when {
                reduceMotion -> fadeOut(tween(MotionTokens.QUICK))
                isTopLevelTransition(initialState, targetState) -> fadeOut(tween(MotionTokens.STANDARD))
                else -> fadeOut(tween(MotionTokens.STANDARD, easing = MotionTokens.Standard)) +
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(MotionTokens.STANDARD, easing = MotionTokens.Standard),
                    )
            }
        },
        popEnterTransition = {
            if (reduceMotion) {
                fadeIn(tween(MotionTokens.QUICK))
            } else {
                fadeIn(tween(MotionTokens.STANDARD, easing = MotionTokens.Standard)) +
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(MotionTokens.STANDARD, easing = MotionTokens.Standard),
                    )
            }
        },
        popExitTransition = {
            if (reduceMotion) {
                fadeOut(tween(MotionTokens.QUICK))
            } else {
                fadeOut(tween(MotionTokens.STANDARD, easing = MotionTokens.Standard)) +
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(MotionTokens.STANDARD, easing = MotionTokens.Standard),
                    )
            }
        },
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToRecoveryFlow = { eventId ->
                    navController.navigate(RelapseRecoveryRoute(relapseJourneyEventId = eventId))
                },
                onNavigateToSettings = { navController.navigate(SettingsRoute) },
                onNavigateToAchievements = { navController.navigate(AchievementsRoute) },
            )
        }
        composable<JourneyRoute> {
            JourneyScreen(
                onNewHabit = { navController.navigate(HabitEditorRoute()) },
                onEditHabit = { id -> navController.navigate(HabitEditorRoute(habitId = id)) },
                onOpenAchievements = { navController.navigate(AchievementsRoute) },
            )
        }
        composable<HabitEditorRoute> {
            HabitEditorScreen(onDone = { navController.popBackStack() })
        }
        composable<LearnRoute> {
            LearnScreen(onOpenArticle = { article -> navController.navigate(LearnArticleRoute(article.name)) })
        }
        composable<LearnArticleRoute> { entry ->
            val route: LearnArticleRoute = entry.toRoute()
            val article = LearnArticle.entries.firstOrNull { it.name == route.articleName }
                ?: LearnArticle.entries.first() // defensive: a route arg can't actually go stale in practice
            LearnArticleScreen(article = article, onBack = { navController.popBackStack() })
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
                onBreathe = { navController.navigate(BreathingRoute()) },
                onBreatheTimed = { seconds -> navController.navigate(BreathingRoute(seconds)) },
                onColdShower = { navController.navigate(ReminderToolRoute(ToolkitTool.COLD_SHOWER.storageValue)) },
                onWalkOutside = { navController.navigate(WalkTimerRoute) },
                onPushUps = { navController.navigate(ReminderToolRoute(ToolkitTool.PUSH_UPS.storageValue)) },
                onDrinkWater = { navController.navigate(ReminderToolRoute(ToolkitTool.DRINK_WATER.storageValue)) },
                onGrounding = {
                    navController.navigate(GuidedStepsRoute(EXERCISE_GROUNDING_CODE))
                },
                onMuscle = {
                    navController.navigate(GuidedStepsRoute(EXERCISE_MUSCLE_CODE))
                },
                onReframe = { navController.navigate(ThoughtRecordEditorRoute()) },
                onCallFriend = { navController.navigate(ReminderToolRoute(ToolkitTool.CALL_FRIEND.storageValue)) },
                onWriteJournal = { navController.navigate(JournalEditorRoute()) },
                onWhy = { navController.navigate(WhyRoute) },
                onDistractionIdeas = { navController.navigate(GuidedStepsRoute(EXERCISE_DISTRACTION_CODE)) },
            )
        }
        composable<BreathingRoute> { entry ->
            val route: BreathingRoute = entry.toRoute()
            BreathingScreen(
                targetDurationSeconds = route.targetDurationSeconds,
                onDone = { navController.popBackStack() },
            )
        }
        composable<ReminderToolRoute> { entry ->
            val route: ReminderToolRoute = entry.toRoute()
            val tool = ToolkitTool.fromStorageValue(route.tool) ?: ToolkitTool.COLD_SHOWER
            ReminderToolScreen(
                tool = tool,
                onDone = { navController.popBackStack() },
            )
        }
        composable<WalkTimerRoute> {
            WalkTimerScreen(onDone = { navController.popBackStack() })
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
                    // seven-step flow sitting on the back stack to return to.
                    navController.popBackStack(HomeRoute, inclusive = false)
                },
                onOpenBreathing = { navController.navigate(BreathingRoute()) },
                onOpenJournal = { navController.navigate(JournalEditorRoute()) },
                onOpenToolkit = { navController.navigate(SosRoute) },
            )
        }
        composable<SettingsRoute> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable<AchievementsRoute> {
            AchievementsScreen(onBack = { navController.popBackStack() })
        }
    }
}

/** NiA-style hierarchy check for bottom-bar selected state. */
fun NavDestination?.isRouteInHierarchy(route: KClass<*>): Boolean =
    this?.hierarchy?.any { it.hasRoute(route) } == true

/** True when both sides of a transition are top-level tabs — see [ClarityNavHost]'s doc comment. */
private fun isTopLevelTransition(initial: NavBackStackEntry, target: NavBackStackEntry): Boolean =
    TopLevelDestination.entries.any { initial.destination.isRouteInHierarchy(it.routeClass) } &&
        TopLevelDestination.entries.any { target.destination.isRouteInHierarchy(it.routeClass) }

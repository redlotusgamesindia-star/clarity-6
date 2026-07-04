package com.runtimelabs.clarity.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector
import com.runtimelabs.clarity.R
import kotlin.reflect.KClass

/**
 * The four bottom-bar tabs. SOS is deliberately NOT one of them — it is a
 * distinct action (the center button) that pushes on top of any tab, so a
 * user in crisis never loses their place.
 */
enum class TopLevelDestination(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val route: Any,
    val routeClass: KClass<*>,
) {
    HOME(
        labelRes = R.string.nav_home,
        icon = Icons.Rounded.Home,
        route = HomeRoute,
        routeClass = HomeRoute::class,
    ),
    JOURNEY(
        labelRes = R.string.nav_journey,
        icon = Icons.Rounded.Insights,
        route = JourneyRoute,
        routeClass = JourneyRoute::class,
    ),
    LEARN(
        labelRes = R.string.nav_learn,
        icon = Icons.Rounded.MenuBook,
        route = LearnRoute,
        routeClass = LearnRoute::class,
    ),
    JOURNAL(
        labelRes = R.string.nav_journal,
        icon = Icons.Rounded.EditNote,
        route = JournalRoute,
        routeClass = JournalRoute::class,
    ),
}

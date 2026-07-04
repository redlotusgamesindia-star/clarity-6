package com.runtimelabs.clarity.navigation

import kotlinx.serialization.Serializable

/*
 * Type-safe Navigation Compose routes. Compile-time checked; route arguments
 * (Phase A: JournalEntryRoute(id), ArticleRoute(slug), ...) become
 * constructor parameters instead of string templates.
 */

@Serializable data object HomeRoute

@Serializable data object JourneyRoute

@Serializable data object LearnRoute

@Serializable data object JournalRoute

/** Urge toolkit. Full-screen, own back stack entry, reachable from anywhere. */
@Serializable data object SosRoute

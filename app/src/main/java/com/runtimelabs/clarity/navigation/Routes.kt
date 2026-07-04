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

/** Journal editor. The default [entryId] of -1 creates a new entry. */
@Serializable data class JournalEditorRoute(val entryId: Long = -1L)

/** Urge toolkit. Full-screen, own back stack entry, reachable from anywhere. */
@Serializable data object SosRoute

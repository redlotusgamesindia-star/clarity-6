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

/** Habit editor. The default [habitId] of -1 creates a new habit. */
@Serializable data class HabitEditorRoute(val habitId: Long = -1L)

/** CBT thought record editor. Default [recordId] of -1 creates a new record. */
@Serializable data class ThoughtRecordEditorRoute(val recordId: Long = -1L)

/** Gratitude editor. Default [entryId] of -1 creates a new entry. */
@Serializable data class GratitudeEditorRoute(val entryId: Long = -1L)

/**
 * Urge/anxiety toolkit hub. Full-screen, own back stack entry, reachable
 * from anywhere via the SOS button.
 */
@Serializable data object SosRoute

@Serializable data object BreathingRoute

/** One of [com.runtimelabs.clarity.feature.toolkit.EXERCISE_GROUNDING] / EXERCISE_MUSCLE. */
@Serializable data class GuidedStepsRoute(val exerciseCode: String)

@Serializable data object WhyRoute

/**
 * The five-step recovery flow shown right after a relapse is confirmed on
 * Home. The relapse event itself is already recorded by the time this
 * opens — this route is entirely about support, never a gate someone must
 * pass through to be "allowed" to move on.
 */
@Serializable data class RelapseRecoveryRoute(val relapseJourneyEventId: Long)

/** Reached via a settings-gear icon on Home, not a bottom-bar tab — see ARCHITECTURE.md §24. */
@Serializable data object SettingsRoute

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

/**
 * [articleName] is a [com.runtimelabs.clarity.domain.learn.LearnArticle] enum
 * constant name — safe to pass directly (never persisted, never deep-linked
 * from outside the app), unlike every DB-backed code elsewhere in this file,
 * which uses a dedicated `storageValue` instead of the enum name.
 */
@Serializable data class LearnArticleRoute(val articleName: String)

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

/** [targetDurationSeconds] of 0 (the default) is open-ended — same sentinel convention as [JournalEditorRoute]. */
@Serializable data class BreathingRoute(val targetDurationSeconds: Int = 0)

/** One of [com.runtimelabs.clarity.feature.toolkit.EXERCISE_GROUNDING] / EXERCISE_MUSCLE / EXERCISE_DISTRACTION. */
@Serializable data class GuidedStepsRoute(val exerciseCode: String)

/** [tool] is a [com.runtimelabs.clarity.domain.toolkit.ToolkitTool.storageValue] — one of the four instruction-and-confirm tools. */
@Serializable data class ReminderToolRoute(val tool: String)

@Serializable data object WalkTimerRoute

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

/** The badge collection. Reached via a trophy icon on Home, same pattern as [SettingsRoute]. */
@Serializable data object AchievementsRoute

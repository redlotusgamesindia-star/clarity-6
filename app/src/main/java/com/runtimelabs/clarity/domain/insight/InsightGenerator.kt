package com.runtimelabs.clarity.domain.insight

import com.runtimelabs.clarity.domain.habit.HabitWindowStats
import com.runtimelabs.clarity.domain.model.DailyCheckIn
import com.runtimelabs.clarity.domain.model.StreakSnapshot
import javax.inject.Inject

/**
 * Typed insights, same contract as plan items: the domain emits stable codes
 * + parameters; the UI resolves localized copy. Copy edits never touch logic.
 */
enum class InsightCode {
    PERFECT_WEEK,
    BEST_HABIT,        // habitName + percent
    FOCUS_HABIT,       // habitName
    CONSISTENCY_UP,    // points delta
    CONSISTENCY_DOWN,
    MOOD_TRENDING_UP,
    URGES_EASING,
    CHECKIN_STREAK,    // days
    MILESTONE_NEAR,    // days remaining
    GETTING_STARTED,
}

data class Insight(
    val code: InsightCode,
    val habitName: String? = null,
    val value: Int? = null,
)

/**
 * Deterministic weekly insights (AD-4): every card traces to one rule below,
 * rules are evaluated in priority order, and at most [MAX_INSIGHTS] surface.
 * Thresholds are deliberately conservative — an insight that fires on noise
 * teaches the user to ignore insights.
 */
class InsightGenerator @Inject constructor() {

    fun generate(
        thisWeek: HabitWindowStats,
        lastWeek: HabitWindowStats,
        checkInsThisWeek: List<DailyCheckIn>,
        checkInsLastWeek: List<DailyCheckIn>,
        streak: StreakSnapshot,
        milestoneDays: Int,
        todayEpochDay: Long,
    ): List<Insight> {
        val insights = mutableListOf<Insight>()

        // 1) Perfect week — the headline when earned.
        if (thisWeek.totalScheduled >= MIN_SCHEDULED_FOR_WEEK_CALLS &&
            thisWeek.totalCompleted == thisWeek.totalScheduled
        ) {
            insights += Insight(InsightCode.PERFECT_WEEK)
        }

        // 2) Milestone proximity — actionable urgency, kindly framed.
        val daysToMilestone = milestoneDays - streak.currentDays
        if (daysToMilestone in 1..MILESTONE_NEAR_WINDOW) {
            insights += Insight(InsightCode.MILESTONE_NEAR, value = daysToMilestone)
        }

        // 3) Week-over-week consistency (both weeks need real sample sizes).
        if (thisWeek.totalScheduled >= MIN_SCHEDULED_FOR_WEEK_CALLS &&
            lastWeek.totalScheduled >= MIN_SCHEDULED_FOR_WEEK_CALLS
        ) {
            val thisPct = thisWeek.totalCompleted * 100 / thisWeek.totalScheduled
            val lastPct = lastWeek.totalCompleted * 100 / lastWeek.totalScheduled
            val delta = thisPct - lastPct
            if (delta >= CONSISTENCY_DELTA_POINTS) {
                insights += Insight(InsightCode.CONSISTENCY_UP, value = delta)
            } else if (delta <= -CONSISTENCY_DELTA_POINTS) {
                insights += Insight(InsightCode.CONSISTENCY_DOWN)
            }
        }

        // 4) Best / focus habit (skip when a perfect week already says it all).
        if (InsightCode.PERFECT_WEEK !in insights.map { it.code }) {
            val rated = thisWeek.perHabit.filter { it.scheduled >= MIN_SCHEDULED_PER_HABIT }
            rated.maxByOrNull { it.percent }
                ?.takeIf { it.percent >= BEST_HABIT_MIN_PERCENT }
                ?.let { insights += Insight(InsightCode.BEST_HABIT, habitName = it.habitName, value = it.percent) }
            rated.minByOrNull { it.percent }
                ?.takeIf { it.percent < FOCUS_HABIT_MAX_PERCENT }
                ?.let { best ->
                    // Don't crown the same habit both best and focus.
                    if (insights.none { it.code == InsightCode.BEST_HABIT && it.habitName == best.habitName }) {
                        insights += Insight(InsightCode.FOCUS_HABIT, habitName = best.habitName)
                    }
                }
        }

        // 5) Mood trend (average, half-point threshold, 3+ samples each week).
        val moodThis = checkInsThisWeek.map { it.mood.storageValue }.averageOrNull()
        val moodLast = checkInsLastWeek.map { it.mood.storageValue }.averageOrNull()
        if (moodThis != null && moodLast != null &&
            checkInsThisWeek.size >= MIN_CHECKINS && checkInsLastWeek.size >= MIN_CHECKINS &&
            moodThis - moodLast >= MOOD_DELTA
        ) {
            insights += Insight(InsightCode.MOOD_TRENDING_UP)
        }

        // 6) Urges easing (a full point quieter on average).
        val urgeThis = checkInsThisWeek.map { it.urgeLevel }.averageOrNull()
        val urgeLast = checkInsLastWeek.map { it.urgeLevel }.averageOrNull()
        if (urgeThis != null && urgeLast != null &&
            checkInsThisWeek.size >= MIN_CHECKINS && checkInsLastWeek.size >= MIN_CHECKINS &&
            urgeLast - urgeThis >= URGE_DELTA
        ) {
            insights += Insight(InsightCode.URGES_EASING)
        }

        // 7) Check-in streak ending today or yesterday.
        val checkInStreak = consecutiveCheckInDays(
            checkInDays = (checkInsThisWeek + checkInsLastWeek).map { it.epochDay }.toSet(),
            todayEpochDay = todayEpochDay,
        )
        if (checkInStreak >= MIN_CHECKIN_STREAK) {
            insights += Insight(InsightCode.CHECKIN_STREAK, value = checkInStreak)
        }

        if (insights.isEmpty()) {
            insights += Insight(InsightCode.GETTING_STARTED)
        }
        return insights.take(MAX_INSIGHTS)
    }

    /** Longest run of consecutive checked-in days ending today (or yesterday). */
    private fun consecutiveCheckInDays(checkInDays: Set<Long>, todayEpochDay: Long): Int {
        var anchor = when {
            todayEpochDay in checkInDays -> todayEpochDay
            (todayEpochDay - 1) in checkInDays -> todayEpochDay - 1
            else -> return 0
        }
        var count = 0
        while (anchor in checkInDays) {
            count++
            anchor--
        }
        return count
    }

    private fun List<Int>.averageOrNull(): Double? = if (isEmpty()) null else average()

    private companion object {
        const val MAX_INSIGHTS = 3
        const val MIN_SCHEDULED_FOR_WEEK_CALLS = 3
        const val MIN_SCHEDULED_PER_HABIT = 3
        const val BEST_HABIT_MIN_PERCENT = 60
        const val FOCUS_HABIT_MAX_PERCENT = 50
        const val CONSISTENCY_DELTA_POINTS = 15
        const val MOOD_DELTA = 0.5
        const val URGE_DELTA = 1.0
        const val MIN_CHECKINS = 3
        const val MIN_CHECKIN_STREAK = 3
        const val MILESTONE_NEAR_WINDOW = 3
    }
}

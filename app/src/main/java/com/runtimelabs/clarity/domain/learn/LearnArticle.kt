package com.runtimelabs.clarity.domain.learn

/**
 * The educational library — the content behind the `Learn` tab, which was
 * a placeholder screen until this polish pass. Bundled, static content,
 * same "no network, ever, except the one documented ads exception" privacy
 * posture as the rest of the app — nothing here is fetched, nothing here
 * reports back what anyone reads.
 *
 * Closed enum + a separate [LearnLibrary] content object, same split as
 * every other content vocabulary in this app (`RecoveryMotivationCode` +
 * `RecoveryMotivationMessages`, `RecoveryChecklistItemCode` +
 * `RecoveryChecklistGenerator`): the domain layer names *which* article,
 * the UI layer resolves *what it says* via string resources, keeping the
 * domain layer locale-free.
 */
enum class LearnArticle(val category: LearnCategory) {
    WHAT_IS_AN_URGE(LearnCategory.UNDERSTANDING_URGES),
    THE_URGE_SURFING_TECHNIQUE(LearnCategory.UNDERSTANDING_URGES),
    WHY_WILLPOWER_ALONE_FALLS_SHORT(LearnCategory.UNDERSTANDING_URGES),
    HALT_YOUR_MOST_COMMON_TRIGGERS(LearnCategory.UNDERSTANDING_URGES),

    HOW_HABITS_ACTUALLY_CHANGE(LearnCategory.BUILDING_NEW_HABITS),
    REPLACING_NOT_JUST_REMOVING(LearnCategory.BUILDING_NEW_HABITS),
    DESIGNING_YOUR_ENVIRONMENT(LearnCategory.BUILDING_NEW_HABITS),
    SLEEP_AND_EXERCISE_AS_INFRASTRUCTURE(LearnCategory.BUILDING_NEW_HABITS),

    WHAT_A_RELAPSE_ACTUALLY_MEANS(LearnCategory.STAYING_THE_COURSE),
    SELF_COMPASSION_ISNT_SELF_INDULGENCE(LearnCategory.STAYING_THE_COURSE),
    WHY_ISOLATION_MAKES_URGES_LOUDER(LearnCategory.STAYING_THE_COURSE),
    PLAYING_THE_TAPE_FORWARD(LearnCategory.STAYING_THE_COURSE),
    ;

    companion object {
        /** Reading order within a category — declaration order, made explicit rather than assumed. */
        fun inCategory(category: LearnCategory): List<LearnArticle> =
            entries.filter { it.category == category }
    }
}

enum class LearnCategory {
    UNDERSTANDING_URGES,
    BUILDING_NEW_HABITS,
    STAYING_THE_COURSE,
    ;

    companion object {
        /** Display order on the Learn screen. */
        val ORDERED: List<LearnCategory> = listOf(UNDERSTANDING_URGES, BUILDING_NEW_HABITS, STAYING_THE_COURSE)
    }
}

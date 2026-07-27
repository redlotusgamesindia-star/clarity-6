package com.runtimelabs.clarity.domain.model

/**
 * Deliberately its own vocabulary, not a reuse of [MoodLevel] — the daily
 * check-in's generic mood scale (Struggling/Low/Okay/Good/Great) isn't the
 * right fit for naming a feeling right after a setback specifically.
 * Guilt, emptiness, anger, anxiety, and hopelessness are the actual shapes
 * that moment tends to take, and naming the real feeling (rather than
 * flattening it to a generic scale) is itself part of the exercise.
 */
enum class RelapseEmotion(val storageValue: String) {
    GUILTY("guilty"),
    EMPTY("empty"),
    ANGRY("angry"),
    ANXIOUS("anxious"),
    HOPELESS("hopeless"),
    OKAY("okay");

    companion object {
        fun fromStorageValue(value: String?): RelapseEmotion? =
            entries.firstOrNull { it.storageValue == value }
    }
}

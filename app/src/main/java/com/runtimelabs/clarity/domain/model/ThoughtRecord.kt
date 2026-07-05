package com.runtimelabs.clarity.domain.model

/**
 * A short CBT-style thought record: what happened, what the mind said, how
 * it felt, and a kinder-but-true reframe. Deliberately the compact four-field
 * form — the full clinical worksheet is homework; this is a two-minute tool.
 */
data class ThoughtRecord(
    val id: Long,
    val epochDay: Long,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val situation: String,
    val automaticThought: String,
    val feeling: String,
    /** 0..10 self-reported intensity of the feeling. */
    val feelingIntensity: Int,
    val reframe: String,
) {
    init {
        require(feelingIntensity in 0..10)
    }

    companion object {
        const val NEW_ID = -1L
    }
}

package com.runtimelabs.clarity.domain.model

/**
 * The classic "three good things" exercise. One is required; pushing for
 * three when someone can only find one would turn gratitude into a test.
 */
data class GratitudeEntry(
    val id: Long,
    val epochDay: Long,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val first: String,
    val second: String?,
    val third: String?,
) {
    companion object {
        const val NEW_ID = -1L
    }
}

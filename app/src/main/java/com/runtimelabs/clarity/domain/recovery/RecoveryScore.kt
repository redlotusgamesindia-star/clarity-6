package com.runtimelabs.clarity.domain.recovery

data class RecoveryScore(
    /** 0..100, the headline number. */
    val percent: Int,
    val totalCleanDays: Int,
    val totalRelapses: Int,
    val bestStreakDays: Int,
    val currentStreakDays: Int,
)

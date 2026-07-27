package com.runtimelabs.clarity.domain.repository

/**
 * Keeps the home-screen widget's cached display state in sync with the
 * truth. No Android/Glance types in this signature on purpose — domain
 * stays platform-free even though the only consumer of this side effect is
 * a platform surface (plan §1).
 */
interface WidgetSyncRepository {
    /**
     * Recomputes the current streak snapshot, writes it to the widget's
     * cache, refreshes any placed widget instances, and re-arms the next
     * scheduled refresh. Idempotent and cheap to call from anywhere.
     */
    suspend fun refresh()
}

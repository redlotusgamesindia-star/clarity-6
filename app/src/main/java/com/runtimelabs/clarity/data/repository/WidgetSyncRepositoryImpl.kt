package com.runtimelabs.clarity.data.repository

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.runtimelabs.clarity.data.local.datastore.WidgetSnapshotStore
import com.runtimelabs.clarity.data.widget.WidgetRefreshScheduler
import com.runtimelabs.clarity.domain.model.JourneyEventType
import com.runtimelabs.clarity.domain.repository.JourneyRepository
import com.runtimelabs.clarity.domain.repository.RecoveryProfileRepository
import com.runtimelabs.clarity.domain.repository.WidgetSyncRepository
import com.runtimelabs.clarity.domain.streak.StreakCalculator
import com.runtimelabs.clarity.widget.ClarityWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class WidgetSyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: RecoveryProfileRepository,
    private val journeyRepository: JourneyRepository,
    private val streakCalculator: StreakCalculator,
    private val widgetSnapshotStore: WidgetSnapshotStore,
    private val widgetRefreshScheduler: WidgetRefreshScheduler,
) : WidgetSyncRepository {

    override suspend fun refresh() {
        // One-shot reads (.first()) — this is a point-in-time sync, not a
        // subscription; the widget has no long-lived collector of its own.
        val profile = profileRepository.profile.first()
        val plan = profileRepository.plan.first()
        val relapseDays = journeyRepository.observeEventDays(JourneyEventType.RELAPSE).first()
        val todayEpochDay = LocalDate.now().toEpochDay()

        val startEpochDay = profile?.createdAtEpochMillis
            ?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay() }
            ?: todayEpochDay

        val streak = streakCalculator.compute(
            recoveryStartEpochDay = startEpochDay,
            relapseEpochDays = relapseDays,
            todayEpochDay = todayEpochDay,
        )
        val milestoneDays = plan?.firstMilestoneDays ?: DEFAULT_MILESTONE_DAYS

        widgetSnapshotStore.write(currentDays = streak.currentDays, milestoneDays = milestoneDays)
        ClarityWidget().updateAll(context)
        widgetRefreshScheduler.scheduleNext()
    }

    private companion object {
        const val DEFAULT_MILESTONE_DAYS = 7
    }
}

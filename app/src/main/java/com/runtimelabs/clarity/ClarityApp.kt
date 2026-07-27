package com.runtimelabs.clarity

import android.app.Application
import com.runtimelabs.clarity.domain.repository.HabitRepository
import com.runtimelabs.clarity.domain.repository.WidgetSyncRepository
import com.runtimelabs.clarity.premium.PremiumManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application entry point. Hilt generates the component tree from here;
 * every @AndroidEntryPoint hangs off this class.
 *
 * Startup work: re-arming reminder alarms, refreshing the home-screen
 * widget, and verifying the premium purchase against Google's own records
 * ([PremiumManager.refreshFromBilling] — this, not any local storage, is
 * the actual mechanism behind "premium survives reinstall": the purchase
 * itself is still valid on Google's servers even when nothing survived on
 * the device, and this call rediscovers it the moment billing connects,
 * with no action required from the person). AlarmManager state dies with
 * force-stop and app updates; a fire-and-forget reconcile on a background
 * dispatcher costs nothing on the critical path and makes all three
 * self-healing. Boot is covered separately by BootCompletedReceiver.
 */
@HiltAndroidApp
class ClarityApp : Application() {

    @Inject
    lateinit var habitRepository: HabitRepository

    @Inject
    lateinit var widgetSyncRepository: WidgetSyncRepository

    @Inject
    lateinit var premiumManager: PremiumManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            habitRepository.rescheduleAllReminders()
            widgetSyncRepository.refresh()
            premiumManager.refreshFromBilling()
        }
    }
}

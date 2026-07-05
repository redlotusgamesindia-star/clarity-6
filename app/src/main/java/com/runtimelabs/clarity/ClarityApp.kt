package com.runtimelabs.clarity

import android.app.Application
import com.runtimelabs.clarity.domain.repository.HabitRepository
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
 * The one piece of startup work: re-arming reminder alarms. AlarmManager
 * state dies with force-stop and app updates; a fire-and-forget reconcile on
 * a background dispatcher costs nothing on the critical path and makes
 * reminders self-healing. Boot is covered separately by BootCompletedReceiver.
 */
@HiltAndroidApp
class ClarityApp : Application() {

    @Inject
    lateinit var habitRepository: HabitRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            habitRepository.rescheduleAllReminders()
        }
    }
}

package com.runtimelabs.clarity.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.runtimelabs.clarity.domain.repository.HabitRepository
import com.runtimelabs.clarity.domain.repository.WidgetSyncRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Alarms don't survive reboot; every enabled reminder AND the widget's
 * daily refresh alarm are re-armed here.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject lateinit var habitRepository: HabitRepository

    @Inject lateinit var widgetSyncRepository: WidgetSyncRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                habitRepository.rescheduleAllReminders()
                widgetSyncRepository.refresh() // also re-arms the widget's daily alarm
            } finally {
                pendingResult.finish()
            }
        }
    }
}

package com.runtimelabs.clarity.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.runtimelabs.clarity.domain.repository.HabitRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Alarms don't survive reboot; every enabled reminder is re-armed here. */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject lateinit var habitRepository: HabitRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                habitRepository.rescheduleAllReminders()
            } finally {
                pendingResult.finish()
            }
        }
    }
}

package com.runtimelabs.clarity.data.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.runtimelabs.clarity.domain.repository.WidgetSyncRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Fires once per day just after midnight. [WidgetSyncRepository.refresh]
 * both updates the widget AND re-arms the next occurrence — same
 * fire-then-rechain idiom as [com.runtimelabs.clarity.data.notifications.HabitReminderReceiver].
 */
@AndroidEntryPoint
class WidgetRefreshReceiver : BroadcastReceiver() {

    @Inject lateinit var widgetSyncRepository: WidgetSyncRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != WidgetRefreshScheduler.ACTION_WIDGET_REFRESH) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                widgetSyncRepository.refresh()
            } finally {
                pendingResult.finish()
            }
        }
    }
}

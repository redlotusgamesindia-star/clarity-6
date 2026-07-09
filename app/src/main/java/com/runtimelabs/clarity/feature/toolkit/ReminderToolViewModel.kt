package com.runtimelabs.clarity.feature.toolkit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.repository.ToolkitUsageRepository
import com.runtimelabs.clarity.domain.toolkit.ToolkitTool
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Backs the four "go do a real-world thing, then confirm" tools: cold
 * shower, push-ups, drink water, call a friend. No live timer — the
 * activity itself happens off-screen, so [onMadeItThrough] records
 * whatever real time elapsed between opening the screen and confirming,
 * which is the only honest signal available here.
 */
@HiltViewModel
class ReminderToolViewModel @Inject constructor(
    private val toolkitUsageRepository: ToolkitUsageRepository,
) : ViewModel() {

    private val startedAtMillis = System.currentTimeMillis()

    fun onMadeItThrough(tool: ToolkitTool) {
        viewModelScope.launch {
            val elapsedSeconds = ((System.currentTimeMillis() - startedAtMillis) / 1_000).toInt()
            toolkitUsageRepository.record(tool, elapsedSeconds.coerceAtLeast(0))
        }
    }
}

package com.runtimelabs.clarity.feature.toolkit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.repository.ToolkitUsageRepository
import com.runtimelabs.clarity.domain.toolkit.ToolkitTool
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** Backs Grounding, Muscle Relaxation, and Distraction Ideas — all the same self-paced step-list shape. */
@HiltViewModel
class GuidedStepsViewModel @Inject constructor(
    private val toolkitUsageRepository: ToolkitUsageRepository,
) : ViewModel() {

    private val startedAtMillis = System.currentTimeMillis()

    fun onFinished(tool: ToolkitTool) {
        viewModelScope.launch {
            val elapsedSeconds = ((System.currentTimeMillis() - startedAtMillis) / 1_000).toInt()
            toolkitUsageRepository.record(tool, elapsedSeconds.coerceAtLeast(0))
        }
    }
}

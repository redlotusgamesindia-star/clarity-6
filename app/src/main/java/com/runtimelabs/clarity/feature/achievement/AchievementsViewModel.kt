package com.runtimelabs.clarity.feature.achievement

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtimelabs.clarity.domain.badge.Badge
import com.runtimelabs.clarity.domain.repository.BadgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** One badge, positioned relative to the person's own progress — never just the bare enum. */
@Immutable
data class BadgeUiModel(
    val badge: Badge,
    val isUnlocked: Boolean,
    /** Null while locked. */
    val unlockedAtEpochDay: Long?,
)

sealed interface AchievementsUiState {
    data object Loading : AchievementsUiState

    /** @Immutable: the `List<BadgeUiModel>` field is otherwise conservatively-unstable — see JournalUiState's fuller rationale. */
    @Immutable
    data class Ready(
        val badges: List<BadgeUiModel>,
        val unlockedCount: Int,
        /** Non-null while a badge's detail sheet is open. */
        val selected: BadgeUiModel?,
    ) : AchievementsUiState {
        val totalCount: Int get() = badges.size
    }
}

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val badgeRepository: BadgeRepository,
) : ViewModel() {

    private val selectedBadge = MutableStateFlow<Badge?>(null)

    val uiState: StateFlow<AchievementsUiState> = combine(
        badgeRepository.observeUnlocked(),
        selectedBadge,
    ) { unlocked, selected ->
        val unlockedByBadge = unlocked.associateBy { it.badge }
        val badges = Badge.entries.map { badge ->
            val entry = unlockedByBadge[badge]
            BadgeUiModel(
                badge = badge,
                isUnlocked = entry != null,
                unlockedAtEpochDay = entry?.unlockedAtEpochDay,
            )
        }
        AchievementsUiState.Ready(
            badges = badges,
            unlockedCount = unlockedByBadge.size,
            selected = selected?.let { code -> badges.firstOrNull { it.badge == code } },
        ) as AchievementsUiState
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AchievementsUiState.Loading,
    )

    fun onBadgeTapped(badge: Badge) {
        selectedBadge.value = badge
    }

    fun onDetailDismissed() {
        selectedBadge.value = null
    }
}

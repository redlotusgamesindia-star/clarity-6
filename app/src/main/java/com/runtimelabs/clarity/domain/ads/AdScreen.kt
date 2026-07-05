package com.runtimelabs.clarity.domain.ads

/**
 * The complete allow-list of screens a banner may appear on — enforced by
 * type, not by a runtime check. This is a deliberate safety choice: a
 * deny-list would mean any future screen shows ads by default unless
 * someone remembers to exclude it. With a closed allow-list, a screen
 * simply has no [AdScreen] value to construct until someone deliberately
 * adds one — fail-closed by construction, which is the only direction that
 * makes sense given this app's subject matter.
 *
 * [SETTINGS] was added exactly this way: a deliberate, one-line addition
 * when the Settings screen shipped, not a rewrite of this enum's shape.
 *
 * [LEARN] exists here ahead of the screen itself (still a placeholder as of
 * this phase) so the allow-list is complete when Learn is actually built —
 * see ARCHITECTURE.md §23. Every current *editor* surface (journal entry,
 * thought record, gratitude, habit editor) and every recovery-sensitive
 * screen (onboarding, breathing, guided steps, the SOS toolkit hub, the
 * relapse recovery flow) is intentionally absent and must stay that way.
 */
enum class AdScreen {
    HOME,
    JOURNEY,
    LEARN,
    JOURNAL_LIST,
    SETTINGS,
}

package com.runtimelabs.clarity.domain.model

enum class RelapseLocation(val storageValue: String) {
    HOME("home"),
    WORK_OR_SCHOOL("work_or_school"),
    TRAVELING("traveling"),
    OTHER("other");

    companion object {
        fun fromStorageValue(value: String?): RelapseLocation? =
            entries.firstOrNull { it.storageValue == value }
    }
}

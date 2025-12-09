package com.domain.model

enum class PlanStatus(val value: String) {
    PENDING("pending"),
    DONE("done"),
    MISSED("missed");

    companion object {
        fun from(value: String?): PlanStatus =
            entries.firstOrNull { it.value == value } ?: PENDING
    }
}

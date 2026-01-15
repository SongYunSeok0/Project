package com.domain.model

data class ChatQueryContext(
    val originalQuery: String,
    val effectiveQuery: String,
    val medicationName: String?
)
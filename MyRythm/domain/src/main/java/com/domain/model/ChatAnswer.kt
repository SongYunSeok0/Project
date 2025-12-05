package com.domain.model

data class ChatAnswer(
    val status: String,
    val question: String,
    val answer: String,
    val contexts: List<ChatContext> = emptyList()
)

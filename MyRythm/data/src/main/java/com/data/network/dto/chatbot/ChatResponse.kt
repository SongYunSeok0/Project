package com.data.network.dto.chatbot

data class ChatResponse(
    val status: String,
    val question: String,
    val result: ChatResult
)

data class ChatResult(
    val answer: String,
    val contexts: List<ChatContextDto> = emptyList()
)
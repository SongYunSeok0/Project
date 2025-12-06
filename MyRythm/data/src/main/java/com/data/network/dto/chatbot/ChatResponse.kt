package com.data.network.dto.chatbot

data class ChatResponse(
    val status: String,
    val task_id: String? = null,
    val question: String? = null,
    val result: ChatResultDto? = null,
    val error: String? = null
)

data class ChatResultDto(
    val answer: String? = null,
    val contexts: List<ChatContextDto>? = emptyList()
)

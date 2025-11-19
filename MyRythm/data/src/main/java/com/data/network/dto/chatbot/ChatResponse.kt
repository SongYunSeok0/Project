// com.data.network.dto.chatbot.ChatResponse.kt
package com.data.network.dto.chatbot

data class ChatResponse(
    val question: String,
    val answer: String,
    val contexts: List<ChatContextDto> = emptyList()
)
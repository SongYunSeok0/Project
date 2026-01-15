package com.chatbot.presentation.model

data class ChatMessage(
    val id: Long,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
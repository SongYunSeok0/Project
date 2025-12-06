package com.data.network.dto.chatbot

data class ChatRequest(
    val question: String,
    val mode: String? = null
)
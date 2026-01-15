package com.chatbot.presentation.model

sealed class ChatEvent {
    data class InputChanged(val text: String) : ChatEvent()
    object SendMessage : ChatEvent()
    object ClearMessages : ChatEvent()
}
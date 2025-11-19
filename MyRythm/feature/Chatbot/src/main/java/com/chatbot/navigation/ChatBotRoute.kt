package com.chatbot.navigation

import kotlinx.serialization.Serializable

@Serializable data object ChatBotNavGraph
@Serializable data class ChatBotRoute(val dummy: String = "")
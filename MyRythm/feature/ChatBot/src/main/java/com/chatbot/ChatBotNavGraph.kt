package com.chatbot

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.navigation.ChatBotRoute

fun NavGraphBuilder.chatbotNavGraph() {
    composable<ChatBotRoute> { ChatBotScreen() }
}

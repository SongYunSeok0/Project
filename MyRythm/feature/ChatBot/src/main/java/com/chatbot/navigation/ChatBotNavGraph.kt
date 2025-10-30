package com.chatbot.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.chatbot.ui.ChatBotScreen

fun NavGraphBuilder.chatbotNavGraph() {
    composable<ChatBotRoute> { ChatBotScreen() }
}

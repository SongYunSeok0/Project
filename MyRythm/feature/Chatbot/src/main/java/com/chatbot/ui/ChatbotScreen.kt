package com.chatbot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chatbot.presentation.model.ChatEvent
import com.chatbot.ui.components.ChatInputField
import com.chatbot.ui.components.ChatMessageList
import com.chatbot.viewmodel.ChatbotViewModel
import com.shared.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun ChatbotScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatbotViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // 자동 스크롤 로직 분리
    AutoScrollEffect(
        messageCount = state.messages.size,
        isLoading = state.loading,
        scrollState = scrollState
    )

    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
        ) {
            ChatMessageList(
                messages = state.messages,
                onResetClick = {
                    viewModel.onEvent(ChatEvent.ClearMessages)
                },
                isLoading = state.loading,
                error = state.error,
                scrollState = scrollState,
                modifier = Modifier.weight(1f)
            )
            ChatInputField(
                input = state.input,
                onQuestionChange = {
                    viewModel.onEvent(ChatEvent.InputChanged(it))
                },
                isLoading = state.loading,
                onSendClick = {
                    viewModel.onEvent(ChatEvent.SendMessage)
                },
            )
        }
    }
}

@Composable
private fun AutoScrollEffect(
    messageCount: Int,
    isLoading: Boolean,
    scrollState: androidx.compose.foundation.ScrollState
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messageCount, isLoading) {
        coroutineScope.launch {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }
}
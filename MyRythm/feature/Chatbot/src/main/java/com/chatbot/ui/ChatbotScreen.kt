package com.chatbot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chatbot.ui.components.ChatInputField
import com.chatbot.ui.components.ChatMessageList
import com.chatbot.viewmodel.ChatbotViewModel
import com.chatbot.viewmodel.ChatbotViewModelFactory
import com.shared.ui.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val viewModel: ChatbotViewModel = viewModel(
        factory = ChatbotViewModelFactory(context)
    )

    val state by viewModel.state.collectAsStateWithLifecycle()

    // 스크롤 상태 및 자동 스크롤
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // 메시지가 추가될 때마다 자동으로 맨 아래로 스크롤
    LaunchedEffect(state.messages.size, state.loading) {
        coroutineScope.launch {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val focusManager = LocalFocusManager.current

    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
        ) {
            ChatMessageList(
                messages = state.messages,
                onResetClick = { viewModel.clearAllMessages() },
                isLoading = state.loading,
                error = state.error,
                scrollState = scrollState,
                modifier = Modifier.weight(1f)
            )
            ChatInputField(
                input = state.input,
                onQuestionChange = { viewModel.onQuestionChange(it) },
                isLoading = state.loading,
                onSendClick = { viewModel.send() },
            )
        }
    }
}
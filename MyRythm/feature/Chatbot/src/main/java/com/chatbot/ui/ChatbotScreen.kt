package com.chatbot.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chatbot.ui.components.ChatInputField
import com.chatbot.ui.components.ChatMessageList
import com.chatbot.viewmodel.ChatbotViewModel
import com.chatbot.viewmodel.ChatbotViewModel.Message
import com.chatbot.viewmodel.ChatbotViewModelFactory
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.components.AppInputField
import com.shared.ui.components.AppMessageCard
import com.shared.ui.components.ChatbotHeader
import com.shared.ui.theme.AppTheme
import com.shared.ui.theme.componentTheme
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

    // 메시지가 추가될 때마다 자동으로 맨 아래스크롤
    LaunchedEffect(state.messages.size, state.loading) {
        coroutineScope.launch {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }
    AppTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { inner ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
            ) {
                // 1229 챗봇 메시지 리스트 영역 컴포넌트 분리
                // 스크롤 반복문 형태는 유지하되 데이터 상태 확인은 기존처럼 뷰모델에서 체크
                ChatMessageList(
                    messages = state.messages,
                    onResetClick = { viewModel.clearAllMessages() },
                    isLoading = state.loading,
                    error = state.error,
                    scrollState = scrollState,
                    modifier = Modifier
                )

                // 1229 챗봇 입력필드 컴포넌트 분리
                // 입력필드는 항상 하단 고정 버전으로 Modifier 입력
                ChatInputField(
                    input = state.input,
                    onQuestionChange = { viewModel.onQuestionChange(it) },
                    isLoading = state.loading,
                    onSendClick = { viewModel.send() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
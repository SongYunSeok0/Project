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
import com.chatbot.viewmodel.ChatbotViewModel
import com.chatbot.viewmodel.ChatbotViewModelFactory
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.components.AppInputField
import com.shared.ui.components.AppMessageCard
import com.shared.ui.components.ChatbotHeader
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

    val sendText = stringResource(R.string.send)
    val errorText = stringResource(R.string.error)
    val exampleText = stringResource(R.string.chat_example)
    val sideEffectReportedText = stringResource(R.string.sideeffectreported)
    val promptStartMessage = stringResource(R.string.chatbot_message_prompt_start)
    val promptQuestionMessage = stringResource(R.string.chatbot_message_prompt_question)
    val contentMessage = stringResource(R.string.chatbot_message_content)
    val answerLoadingMessage = stringResource(R.string.chatbot_message_answer_loading)

    // 스크롤 상태 및 자동 스크롤
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // 메시지가 추가될 때마다 자동으로 맨 아래로 스크롤
    LaunchedEffect(state.messages.size, state.loading) {
        coroutineScope.launch {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            // 메시지 영역
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(top = 12.dp)
            ) {
                // 헤더
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    ChatbotHeader(
                        onResetClick = { viewModel.clearAllMessages() }
                    )
                }

                Spacer(Modifier.height(12.dp))

                Divider(
                    color = MaterialTheme.componentTheme.dividerColor,
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // 메시지 컨텐츠
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    // 초기 안내 메시지
                    AppMessageCard(
                        text = promptStartMessage,
                        isUser = false
                    )

                    Spacer(Modifier.height(8.dp))

                    AppMessageCard(
                        text = sideEffectReportedText,
                        isUser = false
                    ) {
                        Text(
                            exampleText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // 채팅 메시지 목록
                    state.messages.forEach { msg ->
                        AppMessageCard(
                            text = msg.text,
                            isUser = msg.isUser,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .widthIn(max = 280.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // 로딩 상태
                    if (state.loading) {
                        AppMessageCard(
                            text = answerLoadingMessage,
                            textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            isUser = false,
                            alpha = 0.7f
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // 초기 안내 텍스트
                    if (!state.loading && state.messages.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            promptQuestionMessage,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    // 에러 메시지
                    if (state.error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "$errorText ${state.error}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }

                    // ⭐ 입력 필드 공간 확보 (키보드 + 입력 필드 높이)
                    Spacer(Modifier.height(140.dp))
                }
            }

            // 입력 필드 (항상 하단 고정)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 12.dp,
                        bottom = 35.dp
                    )
            ) {
                AppInputField(
                    value = state.input,
                    onValueChange = { viewModel.onQuestionChange(it) },
                    label = contentMessage,
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Send,
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (state.input.isNotBlank() && !state.loading) {
                                viewModel.send()
                            }
                        }
                    ),
                    trailingContent = {
                        AppButton(
                            isCircle = true,
                            width = 44.dp,
                            height = 44.dp,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                if (!state.loading && state.input.isNotBlank()) {
                                    viewModel.send()
                                }
                            }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.send),
                                contentDescription = sendText
                            )
                        }
                    }
                )
            }
        }
    }
}
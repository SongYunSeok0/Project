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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.shared.ui.theme.AppTheme

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

    AppTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
        ) { inner ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
            ) {
                // 스크롤 가능한 전체 채팅 내용
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    ChatbotHeader(
                        onResetClick = { viewModel.clearAllMessages() }
                    )

                    Spacer(Modifier.height(12.dp))

                    // 인사 메시지
                    AppMessageCard(
                        text = promptStartMessage,
                        isUser = false
                    )

                    Spacer(Modifier.height(8.dp))

                    // 예시 질문 안내
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

                    // 채팅 메시지 렌더링 - 사용자 메시지
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

                    // 로딩 메시지
                    if (state.loading) {
                        AppMessageCard(
                            text = answerLoadingMessage,
                            textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            isUser = false,
                            alpha = 0.7f
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // 아무 메시지도 없을 때 안내문
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
                    Spacer(Modifier.height(80.dp))
                }

                // 사용자 입력 필드 + 전송 버튼
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .imePadding()
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
}
package com.chatbot.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chatbot.presentation.model.ChatMessage
import com.shared.R
import com.shared.ui.components.AppMessageCard
import com.shared.ui.components.ChatbotHeader
import com.shared.ui.theme.componentTheme

@Composable
fun ChatMessageList(
    messages: List<ChatMessage>,
    onResetClick: () -> Unit,
    isLoading: Boolean,
    error: String?,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val errorText = stringResource(com.shared.R.string.error)
    val exampleText = stringResource(com.shared.R.string.chat_example)
    val sideEffectReportedText = stringResource(com.shared.R.string.sideeffectreported)
    val promptStartMessage = stringResource(com.shared.R.string.chatbot_message_prompt_start)
    val promptQuestionMessage = stringResource(com.shared.R.string.chatbot_message_prompt_question)
    val answerLoadingMessage = stringResource(R.string.chatbot_message_answer_loading)

    // 메시지 영역
    Column(
        modifier = modifier
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
                onResetClick = onResetClick
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
            messages.forEach { msg ->
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
            if (isLoading) {
                AppMessageCard(
                    text = answerLoadingMessage,
                    textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    isUser = false,
                    alpha = 0.7f
                )
            }

            Spacer(Modifier.height(8.dp))

            // 초기 안내 텍스트
            if (!isLoading && messages.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    promptQuestionMessage,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // 에러 메시지
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "$errorText ${error}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.error
                    )
                )
            }

            Spacer(Modifier.height(140.dp))
        }
    }
}
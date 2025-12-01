package com.chatbot.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chatbot.viewmodel.ChatbotViewModel
import com.chatbot.viewmodel.ChatbotViewModelFactory
import com.shared.R
import com.shared.ui.components.AuthInputField
import com.shared.ui.theme.AuthBackground
import com.shared.ui.theme.InquiryCardAnswer
import com.shared.ui.theme.InquiryCardQuestion
import com.shared.ui.theme.LoginTertiary
import com.shared.ui.theme.OnlyColorTheme
import com.shared.ui.theme.Primary

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

    val chatbotText = stringResource(R.string.chatbot)
    val botIconText = stringResource(R.string.chatbot_icon)
    val chatbotProfile = stringResource(R.string.chatbotprofile)
    val returnToOptionText = stringResource(R.string.return_to_option)
    val errorText = stringResource(R.string.error)
    val exampleText = stringResource(R.string.example)
    val sideEffectReportedText = stringResource(R.string.sideeffectreported)
    val promptStartMessage = stringResource(R.string.chatbot_message_prompt_start)
    val promptQuestionMessage = stringResource(R.string.chatbot_message_prompt_question)
    val contentMessage = stringResource(R.string.chatbot_message_content)
    val answerLoadingMessage = stringResource(R.string.chatbot_message_answer_loading)

    OnlyColorTheme {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { inner ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .background(MaterialTheme.colorScheme.background)
            ) {

                // 스크롤 가능한 전체 채팅 내용
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 상단 헤더
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                botIconText,
                                color = Color.White,
                                fontSize = 16.sp,
                                lineHeight = 1.5.em
                            )
                        }
                        Column {
                            Text(
                                chatbotText,
                                fontSize = 16.sp
                            )
                            Text(
                                chatbotProfile,
                                color = LoginTertiary,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        InputChip(
                            label = {
                                Text(
                                    returnToOptionText,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Image(
                                    painter = painterResource(id = R.drawable.upload),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = InquiryCardQuestion
                            ),
                            selected = true,
                            onClick = {
                                viewModel.clearAllMessages()
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // 인사 메시지
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(AuthBackground)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                promptStartMessage,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // 예시 질문 안내
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(InquiryCardQuestion)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                sideEffectReportedText,
                                fontSize = 14.sp
                            )
                            Text(
                                "$exampleText \"타이레놀 부작용 알려줘\"",
                                color = LoginTertiary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 채팅 메시지 렌더링
                    state.messages.forEach { msg ->
                        val isUser = msg.isUser

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isUser) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            InquiryCardAnswer
                                        }
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    msg.text,
                                    color = Color.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }

                    // 로딩 메시지
                    if (state.loading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(InquiryCardAnswer)
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    answerLoadingMessage,
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // 아무 메시지도 없을 때 안내문
                    if (!state.loading && state.messages.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            promptQuestionMessage,
                            color = LoginTertiary,
                            fontSize = 13.sp
                        )
                    }

                    // 에러 메시지
                    if (state.error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "$errorText ${state.error}",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(Modifier.height(80.dp))
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .imePadding()
                ) {
                    AuthInputField(
                        value = state.input,
                        onValueChange = { viewModel.onQuestionChange(it) },
                        hint = contentMessage,
                        imeAction = ImeAction.Send,
                        shape = RoundedCornerShape(34.dp),
                        modifier = Modifier.weight(1f),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (state.input.isNotBlank() && !state.loading) viewModel.send()
                            }
                        ),
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(Primary)
                                    .clickable(
                                        enabled = !state.loading && state.input.isNotBlank()
                                    ) {
                                        viewModel.send()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.arrow_up),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

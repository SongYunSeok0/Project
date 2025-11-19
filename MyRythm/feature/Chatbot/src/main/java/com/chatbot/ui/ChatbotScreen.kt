package com.chatbot.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chatbot.viewmodel.ChatbotViewModel
import com.chatbot.viewmodel.ChatbotViewModelFactory
import com.common.design.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val viewModel: ChatbotViewModel = viewModel(
        factory = ChatbotViewModelFactory(context)
    )

    val state by viewModel.state.collectAsStateWithLifecycle()

    val chatbotText = stringResource(R.string.chatbot_chatbot)
    val backText = stringResource(R.string.chatbot_back)
    val botIconText = stringResource(R.string.chatbot_bot_icon)
    val chatbotProfile = stringResource(R.string.chatbot_chatbotprofile)
    val returnToOptionText = stringResource(R.string.chatbot_return_to_option)
    val sendText = stringResource(R.string.chatbot_send)
    val errorText = stringResource(R.string.chatbot_error)
    val exampleText = stringResource(R.string.chatbot_example)
    val sideEffectReportedText = stringResource(R.string.chatbot_sideeffectreported)
    val promptStartMessage = stringResource(R.string.chatbot_message_prompt_start)
    val promptQuestionMessage = stringResource(R.string.chatbot_message_prompt_question)
    val contentMessage = stringResource(R.string.chatbot_message_content)
    val answerLoadingMessage = stringResource(R.string.chatbot_message_answer_loading)
    val exampleQuestionMessage = stringResource(R.string.chatbot_message_example_question)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(Color.White)
        ) {

            // 스크롤 가능한 전체 채팅 내용
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                // ===== 헤더 영역 =====
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xff6ae0d9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(botIconText, color = Color.White, fontSize = 16.sp, lineHeight = 1.5.em)
                    }
                    Column {
                        Text(chatbotText, color = Color(0xff5db0a8), fontSize = 16.sp)
                        Text(
                            chatbotProfile,
                            color = Color(0xff4a5565),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    InputChip(
                        label = {
                            Text(returnToOptionText, color = Color(0xff5db0a8), fontSize = 14.sp)
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
                            containerColor = Color(0xffe4f5f4)
                        ),
                        selected = true,
                        onClick = {
                            // 메시지 전체 초기화하고 싶으면 ViewModel에 clearMessages() 만들어서 호출
                            viewModel.onQuestionChange("")
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ===== 인사 메시지 =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xffb5e5e1))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(promptStartMessage, color = Color.Black, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ===== 추천 질문 카드 =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xffb5e5e1))
                            .clickable {
                                val example = exampleQuestionMessage
                                viewModel.onQuestionChange(example)
                                viewModel.send()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(sideEffectReportedText, color = Color.Black, fontSize = 14.sp)
                        Text("$exampleText \"타이레놀 부작용 알려줘\"", color = Color(0xff4a5565), fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ===== 채팅 메시지 렌더링 =====
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
                                    if (isUser) Color(0xff6ae0d9)
                                    else Color(0xfff0fdfb)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(msg.text, color = Color.Black, fontSize = 14.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }

                // ===== 로딩 메시지 =====
                if (state.loading) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xfff0fdfb))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(answerLoadingMessage, color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // 아무 메시지도 없을 때 안내문
                if (!state.loading && state.messages.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        promptQuestionMessage,
                        color = Color(0xff4a5565),
                        fontSize = 13.sp
                    )
                }

                if (state.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text("$errorText ${state.error}", color = Color(0xffe11d48), fontSize = 12.sp)
                }

                Spacer(Modifier.height(80.dp))
            }

            // ===== 하단 입력창 =====
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

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xfff9fafb))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = state.input,
                        onValueChange = { viewModel.onQuestionChange(it) },
                        textStyle = TextStyle(color = Color(0xff111827), fontSize = 14.sp),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (state.input.isEmpty()) {
                                Text(contentMessage, color = Color(0xff99a1af), fontSize = 14.sp)
                            }
                            inner()
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .width(68.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (state.loading || state.input.isBlank())
                                Color(0xffc4f5f0)
                            else
                                Color(0xff6ae0d9)
                        )
                        .clickable(enabled = !state.loading && state.input.isNotBlank()) {
                            viewModel.send()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.upload),
                        contentDescription = sendText,
                        modifier = Modifier.height(20.dp)
                    )
                }
            }
        }
    }
}

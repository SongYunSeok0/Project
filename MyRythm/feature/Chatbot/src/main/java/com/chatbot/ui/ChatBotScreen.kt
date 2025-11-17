package com.chatbot.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chatbot.viewmodel.ChatbotViewModel
import com.chatbot.viewmodel.ChatbotViewModelFactory
import com.common.design.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(
    modifier: Modifier = Modifier,
) {
    // ★ Hilt 대신 우리가 만든 Factory 사용
    val context = LocalContext.current
    val viewModel: ChatbotViewModel = viewModel(
        factory = ChatbotViewModelFactory(context)
    )

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {}   // 탑바 제거
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(Color.White)
        ) {

            // 상단 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .align(Alignment.TopStart)
            ) {
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
                        Text("🤖", color = Color.White, fontSize = 16.sp, lineHeight = 1.5.em)
                    }
                    Column {
                        Text("챗봇", color = Color(0xff5db0a8), fontSize = 16.sp)
                        Text(
                            "AI 약사 의사 응답 모델",
                            color = Color(0xff4a5565),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    InputChip(
                        label = { Text("처음으로", color = Color(0xff5db0a8), fontSize = 14.sp) },
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
                            // 질문만 초기화
                            viewModel.onQuestionChange("")
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 인사/가이드 카드
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xffb5e5e1))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        "안녕하세요! AI 약사입니다.\n무엇을 도와드릴까요?",
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                // 추천 질문 카드
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xffb5e5e1))
                        .clickable {
                            val example = "타이레놀 부작용 알려줘"
                            viewModel.onQuestionChange(example)
                            viewModel.send()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        "부작용 확인",
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        "예: \"타이레놀 부작용 알려줘\"",
                        color = Color(0xff4a5565),
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                // 답변 카드
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xfff0fdfb))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    val answerText =
                        when {
                            state.loading -> "답변을 불러오는 중입니다..."
                            state.answer.isNotBlank() -> state.answer
                            else -> "약, 부작용, 복용법, 증상, 병원 찾기 등에 대해 자유롭게 질문해보세요."
                        }

                    Text(
                        answerText,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }

                if (state.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "오류: ${state.error}",
                        color = Color(0xffe11d48),
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(80.dp))
            }

            // 하단 입력창
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
                        value = state.question,
                        onValueChange = { viewModel.onQuestionChange(it) },
                        textStyle = TextStyle(
                            color = Color(0xff111827),
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (state.question.isEmpty()) {
                                Text(
                                    "메시지를 입력하세요...",
                                    color = Color(0xff99a1af),
                                    fontSize = 14.sp
                                )
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
                            if (state.loading || state.question.isBlank())
                                Color(0xffc4f5f0)
                            else
                                Color(0xff6ae0d9)
                        )
                        .clickable(
                            enabled = !state.loading && state.question.isNotBlank()
                        ) {
                            viewModel.send()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.upload),
                        contentDescription = "send",
                        modifier = Modifier.height(20.dp)
                    )
                }
            }
        }
    }
}

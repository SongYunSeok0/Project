package com.chatbot.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.imePadding
import com.common.design.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "챗봇",
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontSize = 16.sp, letterSpacing = 1.sp)
                    )
                },
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.upload),
                        contentDescription = "back",
                        modifier = Modifier.size(22.dp) // 음수 패딩 제거
                    )
                }
            )
        }
    ) { inner ->
        // 콘텐츠 + 하단 입력창(Scaffold bottomBar 미사용)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(Color.White)
        ) {
            // 상단 영역(헤더/칩/가이드 등)
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
                        Text("챗봇", color = Color(0xff5db0a8), fontSize = 16.sp, lineHeight = 1.5.em)
                        Text("AI 약사 의사 응답 모델", color = Color(0xff4a5565), fontSize = 14.sp, lineHeight = 1.43.em)
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
                        colors = FilterChipDefaults.filterChipColors(containerColor = Color(0xffe4f5f4)),
                        selected = true,
                        onClick = {}
                    )
                }

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xffb5e5e1))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        "안녕하세요! AI 약사입니다.\n무엇을 도와드릴까요?",
                        color = Color.Black,
                        fontSize = 14.sp,
                        lineHeight = 1.43.em
                    )
                }

                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xffb5e5e1))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("부작용 확인", color = Color.Black, fontSize = 14.sp, lineHeight = 1.43.em)
                }

                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xffb5e5e1))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("부작용 확인에 대해 질문해주세요.", color = Color.Black, fontSize = 14.sp, lineHeight = 1.43.em)
                }

                Spacer(Modifier.height(80.dp)) // 입력창 공간 확보
            }

            // 하단 고정 입력창
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .imePadding() // 키보드 올라올 때 자동 보정
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xfff9fafb))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("메시지를 입력하세요...", color = Color(0xff99a1af), fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .width(68.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xff6ae0d9)),
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

@Preview(widthDp = 412, heightDp = 917)
@Composable
private fun ChatBotScreenPreview() {
    ChatBotScreen()
}

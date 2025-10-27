package com.example.chatbot

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.example.common.design.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .requiredWidth(width = 412.dp)
            .requiredHeight(height = 917.dp)
            .background(color = Color.White)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Box(
                            modifier = Modifier
                                .requiredWidth(width = 85.dp)
                                .requiredHeight(height = 23.dp)
                        ) {
                            Text(
                                text = "챗봇",
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    letterSpacing = 1.sp),
                                modifier = Modifier
                                    .align(alignment = Alignment.TopStart)
                                    .offset(x = 27.63.dp,
                                        y = 3.7.dp))
                        }
                    },
                    navigationIcon = {
                        Column(
                            modifier = Modifier
                                .requiredWidth(width = 20.dp)
                                .requiredHeight(height = 20.dp)
                                .padding(start = -0.9953689575195312.dp,
                                    end = -0.9953727722167969.dp,
                                    top = -0.9953689575195312.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .requiredHeight(height = 22.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.back),
                                    contentDescription = "Icon",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .requiredHeight(height = 22.dp))
                            }
                        }
                    })
            }
        ) {
                innerPadding ->
            val contentModifier = Modifier.padding(innerPadding)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .requiredWidth(width = 413.dp)
                    .requiredHeight(height = 77.dp)
                    .background(color = Color.White)
                    .padding(start = 23.993059158325195.dp,
                        end = 192.013916015625.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(11.99.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .requiredWidth(width = 197.dp)
                        .requiredHeight(height = 44.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .requiredSize(size = 40.dp)
                            .clip(shape = RoundedCornerShape(24855100.dp))
                            .background(color = Color(0xff6ae0d9))
                            .padding(end = 0.011575698852539062.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .requiredWidth(width = 22.dp)
                                .requiredHeight(height = 24.dp)
                        ) {
                            Text(
                                text = "🤖",
                                color = Color.White,
                                lineHeight = 1.5.em,
                                style = TextStyle(
                                    fontSize = 16.sp),
                                modifier = Modifier
                                    .align(alignment = Alignment.TopStart)
                                    .offset(x = 0.dp,
                                        y = (-1.52).dp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .requiredWidth(width = 145.dp)
                            .requiredHeight(height = 44.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .requiredWidth(width = 145.dp)
                                .requiredHeight(height = 24.dp)
                        ) {
                            Text(
                                text = "챗봇",
                                color = Color(0xff5db0a8),
                                lineHeight = 1.5.em,
                                style = TextStyle(
                                    fontSize = 16.sp),
                                modifier = Modifier
                                    .align(alignment = Alignment.TopStart)
                                    .offset(x = 0.dp,
                                        y = (-1.52).dp))
                        }
                        Row(
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 0.dp,
                                    y = 23.99.dp)
                                .requiredWidth(width = 145.dp)
                                .requiredHeight(height = 20.dp)
                        ) {
                            Text(
                                text = "AI 약사 의사 응답 모델",
                                color = Color(0xff4a5565),
                                lineHeight = 1.43.em,
                                style = TextStyle(
                                    fontSize = 14.sp),
                                modifier = Modifier
                                    .fillMaxWidth())
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .requiredWidth(width = 413.dp)
                    .requiredHeight(height = 274.dp)
            ) {
                Column(
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 23.99.dp,
                            y = 16.dp)
                        .requiredWidth(width = 202.dp)
                        .requiredHeight(height = 64.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(color = Color(0xffb5e5e1))
                        .padding(start = 15.995370864868164.dp,
                            end = 15.995382308959961.dp,
                            top = 11.990753173828125.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(height = 40.dp)
                    ) {
                        Text(
                            text = "안녕하세요! AI 약사입니다.\n무엇을 도와드릴까요?",
                            color = Color.Black,
                            lineHeight = 1.43.em,
                            style = TextStyle(
                                fontSize = 14.sp),
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 0.dp,
                                    y = (-2).dp)
                                .requiredWidth(width = 170.dp))
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(11.99.dp, Alignment.Top),
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 23.99.dp,
                            y = 95.97.dp)
                        .requiredWidth(width = 365.dp)
                        .requiredHeight(height = 162.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(height = 56.dp)
                            .clip(shape = RoundedCornerShape(10.dp))
                            .background(color = Color(0xffe4f5f4))
                            .padding(start = 15.995370864868164.dp,
                                end = 15.995382308959961.dp,
                                top = 15.995376586914062.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .requiredHeight(height = 24.dp)
                        ) {
                            Text(
                                text = "어떤 것을 질문하시겠습니까?",
                                color = Color(0xff101828),
                                lineHeight = 1.5.em,
                                style = TextStyle(
                                    fontSize = 16.sp),
                                modifier = Modifier
                                    .align(alignment = Alignment.TopStart)
                                    .offset(x = 0.dp,
                                        y = (-1.52).dp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(height = 94.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xff6ae0d9),
                            border = BorderStroke(1.4814800024032593.dp, Color(0xff6ae0d9)),
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(10.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredWidth(width = 110.dp)
                                    .requiredHeight(height = 43.dp)
                            ) {
                                Text(
                                    text = "의약품 질문",
                                    color = Color.White,
                                    lineHeight = 1.43.em,
                                    style = TextStyle(
                                        fontSize = 14.sp),
                                    modifier = Modifier
                                        .align(alignment = Alignment.TopStart)
                                        .offset(x = 17.48.dp,
                                            y = 9.47.dp))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.4814800024032593.dp, Color(0xff5db0a8)),
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 117.84.dp,
                                    y = 0.dp)
                                .clip(shape = RoundedCornerShape(10.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredWidth(width = 77.dp)
                                    .requiredHeight(height = 43.dp)
                            ) {
                                Text(
                                    text = "복용법",
                                    color = Color(0xff5db0a8),
                                    lineHeight = 1.43.em,
                                    style = TextStyle(
                                        fontSize = 14.sp),
                                    modifier = Modifier
                                        .align(alignment = Alignment.TopStart)
                                        .offset(x = 17.48.dp,
                                            y = 9.47.dp))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.4814800024032593.dp, Color(0xff5db0a8)),
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 202.77.dp,
                                    y = 0.dp)
                                .clip(shape = RoundedCornerShape(10.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredWidth(width = 110.dp)
                                    .requiredHeight(height = 43.dp)
                            ) {
                                Text(
                                    text = "부작용 확인",
                                    color = Color(0xff5db0a8),
                                    lineHeight = 1.43.em,
                                    style = TextStyle(
                                        fontSize = 14.sp),
                                    modifier = Modifier
                                        .align(alignment = Alignment.TopStart)
                                        .offset(x = 17.48.dp,
                                            y = 9.47.dp))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.4814800024032593.dp, Color(0xff5db0a8)),
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 0.dp,
                                    y = 50.94.dp)
                                .clip(shape = RoundedCornerShape(10.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredWidth(width = 124.dp)
                                    .requiredHeight(height = 43.dp)
                            ) {
                                Text(
                                    text = "약물 상호작용",
                                    color = Color(0xff5db0a8),
                                    lineHeight = 1.43.em,
                                    style = TextStyle(
                                        fontSize = 14.sp),
                                    modifier = Modifier
                                        .align(alignment = Alignment.TopStart)
                                        .offset(x = 17.48.dp,
                                            y = 9.47.dp))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.4814800024032593.dp, Color(0xff5db0a8)),
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 131.83.dp,
                                    y = 50.94.dp)
                                .clip(shape = RoundedCornerShape(10.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredWidth(width = 63.dp)
                                    .requiredHeight(height = 43.dp)
                            ) {
                                Text(
                                    text = "기타",
                                    color = Color(0xff5db0a8),
                                    lineHeight = 1.43.em,
                                    style = TextStyle(
                                        fontSize = 14.sp),
                                    modifier = Modifier
                                        .align(alignment = Alignment.TopStart)
                                        .offset(x = 17.48.dp,
                                            y = 9.47.dp))
                            }
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .requiredWidth(width = 413.dp)
                    .requiredHeight(height = 77.dp)
                    .background(color = Color.White)
                    .padding(start = 23.993059158325195.dp,
                        end = 23.99305534362793.dp,
                        top = 16.736114501953125.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(height = 44.dp)
                        .padding(end = 0.0000171661376953125.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .requiredHeight(height = 44.dp)
                            .weight(weight = 1f)
                            .clip(shape = RoundedCornerShape(10.dp))
                            .background(color = Color(0xfff9fafb))
                            .padding(horizontal = 16.dp,
                                vertical = 12.dp)
                    ) {
                        Text(
                            text = "메시지를 입력하세요...",
                            color = Color(0xff99a1af),
                            style = TextStyle(
                                fontSize = 14.sp))
                    }
                    Column(
                        modifier = Modifier
                            .requiredWidth(width = 68.dp)
                            .requiredHeight(height = 44.dp)
                            .clip(shape = RoundedCornerShape(10.dp))
                            .background(color = Color(0xff6ae0d9))
                            .padding(start = 23.993072509765625.dp,
                                end = 23.9930477142334.dp,
                                top = 11.990753173828125.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.upload),
                            contentDescription = "Icon",
                            modifier = Modifier
                                .fillMaxWidth()
                                .requiredHeight(height = 20.dp))
                    }
                }
            }

        }
    }
}

@Preview(widthDp = 412, heightDp = 917)
@Composable
private fun ChatbotScreenPreview() {
    ChatbotScreen(Modifier)
}
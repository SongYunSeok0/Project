package com.example.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
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
import com.example.common.design.R

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    // 사용할 임시 벡터 리소스 ID (실제 아이콘으로 교체 필요)
    val tempIconResId = R.drawable.logo // 또는 다른 실제 drawable 리소스

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xfffcfcfc))
                .padding(bottom = 58.dp)
        ) {
            // 상단 바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(color = Color.White)
            )
            // 메인 콘텐츠
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(color = Color(0xfffcfcfc))
            ) {
                Column(
                    modifier = Modifier
                        .requiredWidth(width = 396.dp)
                        .requiredHeight(height = 77.dp)
                        .background(color = Color.White)
                        .padding(start = 24.dp,
                            end = 24.dp,
                            top = 16.dp,
                            bottom = 0.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(height = 36.dp)
                    ) {
                        Image(
                            painter = painterResource(id = tempIconResId),
                            contentDescription = "Icon",
                            modifier = Modifier
                                .requiredSize(size = 24.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 0.dp,
                            y = 76.72.dp)
                        .width(396.dp)
                        .height(872.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 23.99.dp,
                                y = 23.99.dp)
                            .width(348.dp)
                            .height(128.dp)
                    ) {
                        // 챗봇
                        Column(
                            verticalArrangement = Arrangement.spacedBy(11.99.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(166.dp)
                                .height(128.dp)
                                .clip(shape = RoundedCornerShape(14.dp))
                                .background(color = Color(0xffe8f5f4))
                                .border(border = BorderStroke(0.74.dp, Color(0xfff3f4f6)), shape = RoundedCornerShape(14.dp))
                        ) {
                            Image(painter = painterResource(id = tempIconResId), contentDescription = "RobotIcon", modifier = Modifier.requiredSize(size = 32.dp))
                            Text(text = "챗봇", color = Color(0xff1e2939), lineHeight = 1.43.em, style = TextStyle(fontSize = 14.sp))
                        }
                        // 스케줄러
                        Column(
                            verticalArrangement = Arrangement.spacedBy(11.99.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 181.78.dp, y = 0.dp)
                                .width(166.dp)
                                .height(128.dp)
                                .clip(shape = RoundedCornerShape(14.dp))
                                .background(color = Color(0xfff0e8f5))
                                .border(border = BorderStroke(0.74.dp, Color(0xfff3f4f6)), shape = RoundedCornerShape(14.dp))
                        ) {
                            Image(painter = painterResource(id = tempIconResId), contentDescription = "Icon", modifier = Modifier.requiredSize(size = 32.dp))
                            Text(text = "스케줄러", color = Color(0xff1e2939), lineHeight = 1.43.em, style = TextStyle(fontSize = 14.sp))
                        }
                    }

                    // 걸음수/심박수 블록
                    Box(
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 23.99.dp, y = 168.28.dp)
                            .width(348.dp)
                            .height(128.dp)
                    ) {
                        // 걸음수
                        Column(
                            verticalArrangement = Arrangement.spacedBy(11.99.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(166.dp)
                                .height(128.dp)
                                .clip(shape = RoundedCornerShape(14.dp))
                                .background(color = Color(0xffe8f0f5))
                                .border(border = BorderStroke(0.74.dp, Color(0xfff3f4f6)), shape = RoundedCornerShape(14.dp))
                        ) {
                            Image(painter = painterResource(id = tempIconResId), contentDescription = "Icon", modifier = Modifier.requiredSize(size = 32.dp))
                            Text(text = "걸음수", color = Color(0xff1e2939), lineHeight = 1.43.em, style = TextStyle(fontSize = 14.sp))
                        }
                        // 최근 심박 수
                        Column(
                            verticalArrangement = Arrangement.spacedBy(11.99.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 181.78.dp, y = 0.dp)
                                .width(166.dp)
                                .height(128.dp)
                                .clip(shape = RoundedCornerShape(14.dp))
                                .background(color = Color(0xffffe8e8))
                                .border(border = BorderStroke(0.74.dp, Color(0xfff3f4f6)), shape = RoundedCornerShape(14.dp))
                        ) {
                            Image(painter = painterResource(id = tempIconResId), contentDescription = "Icon", modifier = Modifier.requiredSize(size = 32.dp))
                            Text(text = "최근 심박 수", color = Color(0xff1e2939), lineHeight = 1.43.em, style = TextStyle(fontSize = 14.sp))
                        }
                    }

                    // 복용까지 남은 시간 블록
                    Column(
                        verticalArrangement = Arrangement.spacedBy(11.99.dp, Alignment.Top),
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 23.99.dp, y = 312.56.dp)
                            .width(348.dp)
                            .height(180.dp)
                            .clip(shape = RoundedCornerShape(14.dp))
                            .background(color = Color(0xffb5d8f5))
                            .padding(24.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                        ) {
                            Text(text = "복용까지 남은 시간", color = Color(0xff1e2939), lineHeight = 1.5.em, style = TextStyle(fontSize = 16.sp))
                            Image(painter = painterResource(id = tempIconResId), contentDescription = "Icon", modifier = Modifier.requiredSize(size = 24.dp))
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(96.dp)
                                .padding(top = 16.dp)
                        ) {
                            Text(text = "2:30", color = Color(0xff1e2939), textAlign = TextAlign.Center, lineHeight = 1.11.em, style = MaterialTheme.typography.displaySmall)
                            Text(text = "10분 전 알림 예정", color = Color(0xff4a5565), textAlign = TextAlign.Center, lineHeight = 1.43.em, style = TextStyle(fontSize = 14.sp))
                        }
                    }

                    // 지도/뉴스 블록
                    Box(
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 23.99.dp, y = 508.5.dp)
                            .width(348.dp)
                            .height(128.dp)
                    ) {
                        // 지도
                        Column(
                            verticalArrangement = Arrangement.spacedBy(11.99.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(166.dp)
                                .height(128.dp)
                                .clip(shape = RoundedCornerShape(14.dp))
                                .background(color = Color(0xffe8f5f0))
                                .border(border = BorderStroke(0.74.dp, Color(0xfff3f4f6)), shape = RoundedCornerShape(14.dp))
                        ) {
                            Image(painter = painterResource(id = tempIconResId), contentDescription = "Icon", modifier = Modifier.requiredSize(size = 32.dp))
                            Text(text = "지도", color = Color(0xff1e2939), lineHeight = 1.43.em, style = TextStyle(fontSize = 14.sp))
                        }
                        // 뉴스
                        Column(
                            verticalArrangement = Arrangement.spacedBy(11.99.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 181.78.dp, y = 0.dp)
                                .width(166.dp)
                                .height(128.dp)
                                .clip(shape = RoundedCornerShape(14.dp))
                                .background(color = Color(0xfffff4e8))
                                .border(border = BorderStroke(0.74.dp, Color(0xfff3f4f6)), shape = RoundedCornerShape(14.dp))
                        ) {
                            Image(painter = painterResource(id = tempIconResId), contentDescription = "Icon", modifier = Modifier.requiredSize(size = 32.dp))
                            Text(text = "뉴스", color = Color(0xff1e2939), lineHeight = 1.43.em, style = TextStyle(fontSize = 14.sp))
                        }
                    }

                    // 건강 인사이트 블록
                    Column(
                        verticalArrangement = Arrangement.spacedBy(11.99.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 23.99.dp, y = 652.78.dp)
                            .width(348.dp)
                            .height(195.dp)
                            .clip(shape = RoundedCornerShape(14.dp))
                            .background(color = Color(0xffe8f5f4))
                    ) {
                        Image(painter = painterResource(id = tempIconResId), contentDescription = "Icon", modifier = Modifier.requiredSize(size = 48.dp))
                        Text(text = "건강 인사이트", color = Color(0xff1e2939), lineHeight = 1.5.em, style = TextStyle(fontSize = 16.sp))
                        Text(text = "오늘의 건강 데이터를 확인하세요", color = Color(0xff4a5565), textAlign = TextAlign.Center, lineHeight = 1.43.em, style = TextStyle(fontSize = 14.sp))
                    }
                }
            }
        }

        // 하단 네비게이션 바
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(58.dp)
                .background(Color.White.copy(alpha = 0.48f))
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 103.15.dp, y = 0.dp)
                    .requiredSize(size = 24.dp)
            ) {
                Image(
                    painter = painterResource(id = tempIconResId),
                    contentDescription = "HomeIcon",
                    modifier = Modifier.requiredSize(size = 24.dp))
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-127.55).dp, y = 0.dp)
                    .requiredWidth(width = 24.dp)
                    .requiredHeight(height = 28.dp)
            ) {
                Image(
                    painter = painterResource(id = tempIconResId),
                    contentDescription = "ProfileIcon",
                    modifier = Modifier
                        .requiredWidth(width = 24.dp)
                        .requiredHeight(height = 28.dp))
            }
            // FAB 버튼 (중앙)
            FloatingActionButton(
                onClick = { },
                containerColor = Color(0xff6ae0d9).copy(alpha = 0.5f),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = 0.dp, y = (-23.99).dp)
            ) {
                Image(
                    painter = painterResource(id = tempIconResId),
                    contentDescription = "Icon",
                    modifier = Modifier.requiredSize(size = 32.dp))
            }
        }
    }
}

@Preview(widthDp = 412, heightDp = 1090)
@Composable
fun MainScreenPreview() {
    MainScreen(Modifier)
}
package com.example.main

/*
package com.sesac.t

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sesac.t.ui.theme.TTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height // Modifier.height 사용을 위해 추가
import androidx.compose.foundation.layout.width // Modifier.width 사용을 위해 추가

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GeneratedCode(
                        // innerPadding을 GeneratedCode의 Modifier에 적용
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GeneratedCode(modifier: Modifier = Modifier) {
    // 사용할 임시 벡터 리소스 ID
    val tempIconResId = R.drawable.ic_android_black_24dp

    Box(
        modifier = modifier
            .fillMaxSize() // 최상위 Box는 fillMaxSize를 사용 (requiredWidth/Height 제거)
            .background(color = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Column도 fillMaxSize를 사용하거나, 필요한 만큼만 height 지정
                .background(color = Color(0xfffcfcfc))
                .padding(bottom = 58.dp) // 하단 FAB/네비게이션 바 공간 확보
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
                    .weight(1f) // 남은 공간을 모두 차지
                    .background(color = Color(0xfffcfcfc))
            ) {
                // ... (메인 콘텐츠 레이아웃은 그대로 유지) ...

                // 기존의 Absolute Positioning 로직을 유지
                Column(
                    modifier = Modifier
                        .requiredWidth(width = 396.dp)
                        .requiredHeight(height = 77.dp)
                        .background(color = Color.White)
                        .padding(start = 24.dp, // 하드코딩된 소수점 대신 깔끔한 dp 사용 권장
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

        // 1. FloatingActionButton을 메인 Column 밖으로 이동시키고 하단에 고정합니다.
        // 이 부분은 BottomAppBar로 바꾸는 것이 Compose 관례에 더 맞습니다.
        // 현재 로직을 최대한 유지하기 위해 Box에 고정합니다.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter) // 하단 중앙에 정렬
                .fillMaxWidth()
                .height(58.dp) // FAB 컨테이너의 높이
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
                    .offset(x = (-127.55).dp, y = 0.dp) // (413/2) - 285.45 = 127.55
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
            // 실제 FAB 버튼 (중앙)
            FloatingActionButton(
                onClick = { },
                containerColor = Color(0xff6ae0d9).copy(alpha = 0.5f),
                shape = RoundedCornerShape(24855100.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter) // 컨테이너의 TopCenter에 위치
                    .offset(x = 0.dp, y = (-23.99).dp) // 위로 튀어나오게 조정
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
fun GeneratedCodePreview() {
    TTheme {
        GeneratedCode(Modifier)
    }
}
 */
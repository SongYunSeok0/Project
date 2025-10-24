package com.example.scheduler

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
fun CameraScreen(modifier: Modifier = Modifier) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "처방전 인식",
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontSize = 16.sp, letterSpacing = 1.sp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* ← 여기에 뒤로가기 동작 */ }) {
                        Image(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "뒤로가기",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Color(0xFF6AE0D9))
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFE4F5F4)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),           // 좌우 여백
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 프리뷰 박스
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF9F9F9),
                border = BorderStroke(1.66.dp, Color(0xFF6AE0D9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(459.dp)
                ) {
                    // 카메라 심볼
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(456.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.camera),
                            contentDescription = "Camera Icon",
                            modifier = Modifier.size(48.dp),
                            colorFilter = ColorFilter.tint(Color(0xFF6AE0D9))
                        )
                    }
                    // 안쪽 테두리 프레임
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 34.dp)
                            .width(277.dp)
                            .height(392.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                border = BorderStroke(1.66.dp, Color.White),
                                shape = RoundedCornerShape(10.dp)
                            )
                    )
                    // 안내 텍스트 칩
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .width(243.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                    ) {
                        Text(
                            text = "처방전을 프레임 안에 맞춰주세요",
                            color = Color(0xFF3B566E),
                            textAlign = TextAlign.Center,
                            lineHeight = 1.43.em,
                            style = TextStyle(fontSize = 14.sp),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // 가이드 카드
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE4F5F4))
                    .padding(16.dp)
            ) {
                Text(
                    text = "📋 촬영 가이드",
                    color = Color(0xFF5DB0A8),
                    lineHeight = 1.43.em,
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "• 밝은 곳에서 촬영하면 인식률이 높아집니다",
                    color = Color(0xFF6F8BA4),
                    lineHeight = 1.43.em,
                    style = TextStyle(fontSize = 14.sp)
                )
                Text(
                    text = "• 글씨가 선명하게 보이는지 확인해주세요",
                    color = Color(0xFF6F8BA4),
                    lineHeight = 1.43.em,
                    style = TextStyle(fontSize = 14.sp)
                )
            }

            // 버튼 영역
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 촬영 버튼
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF6AE0D9))
                        .shadow(1.dp, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Camera Icon",
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "사진 촬영",
                        color = Color.White,
                        lineHeight = 1.5.em,
                        style = TextStyle(fontSize = 16.sp)
                    )
                }

                // 갤러리 선택 버튼
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(59.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF6AE0D9))
                        .shadow(1.dp, RoundedCornerShape(14.dp))
                        .border(
                            BorderStroke(1.66.dp, Color(0xFF6AE0D9)),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.upload),
                        contentDescription = "Upload Icon",
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(Color(0xFFFFFFFF))
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "갤러리에서 선택",
                        color = Color(0xFFFFFFFF),
                        lineHeight = 1.5.em,
                        style = TextStyle(fontSize = 16.sp)
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 392, heightDp = 917, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CameraScreenPreview() {
    CameraScreen()
}

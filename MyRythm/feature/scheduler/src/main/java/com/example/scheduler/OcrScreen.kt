package com.example.scheduler

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.common.design.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionScanScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
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
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "뒤로가기",
                            tint = Color(0xFF6AE0D9),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFE4F5F4)
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            // 미리보기 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(486.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF3F4F6))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.upload), // 플레이스홀더
                    contentDescription = "preview",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.fillMaxSize()
                )
                // 우상단 원형 액션 버튼
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(36.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF6AE0D9)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.upload),
                        contentDescription = "action",
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }

            // 안내 배너
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFE4F5F4),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "처방전이 선명하게 보이나요?",
                        color = Color(0xFF5DB0A8),
                        lineHeight = 1.43.em,
                        style = TextStyle(fontSize = 14.sp)
                    )
                }
            }

            // 하단 버튼들
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 확인 및 등록
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF6AE0D9),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "확인 및 등록",
                            color = Color.White,
                            lineHeight = 1.5.em,
                            style = TextStyle(fontSize = 16.sp)
                        )
                    }
                }

                // 다시 촬영 (외곽선 버튼)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFF6AE0D9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "다시 촬영",
                            color = Color(0xFF6AE0D9),
                            lineHeight = 1.5.em,
                            style = TextStyle(fontSize = 16.sp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 412, heightDp = 851, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PrescriptionScanScreenPreview() {
    PrescriptionScanScreen()
}

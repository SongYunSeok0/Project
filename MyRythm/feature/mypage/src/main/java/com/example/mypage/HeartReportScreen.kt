package com.example.mypage

import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.common.design.R // R.drawable.heart, R.drawable.arrow, R.drawable.line 등 리소스는 common 모듈에서 가져옵니다.


@Composable
fun HeartRateScreen() {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally // 전체 Column 중앙 정렬
            ) {
                // 심박수 카드
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(296.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xffffe8e8), Color(0xffffd5d5))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally, // 카드 내부 중앙 정렬
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.heart),
                            contentDescription = "Heart Icon",
                            alpha = 0.66f,
                            colorFilter = ColorFilter.tint(Color(0xffff6b6b)),
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "현재 심박수",
                            color = Color(0xff4a5565),
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "178",
                            color = Color(0xff101828),
                            fontSize = 60.sp
                        )

                        Text(
                            text = "BPM",
                            color = Color(0xff4a5565),
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        InputChip(
                            label = { Text("정상", fontSize = 14.sp, color = Color(0xff364153)) },
                            shape = RoundedCornerShape(50.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.5f)
                            ),
                            selected = true,
                            onClick = {}
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 심박수 측정 버튼
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xff6ae0d9)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = "Arrow",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("심박수 측정하기", color = Color.White, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 최근 측정 기록 제목
                Text(
                    text = "최근 측정 기록",
                    fontSize = 16.sp,
                    color = Color(0xff101828)
                )

                // 여기에 기록 리스트 추가 가능
            }
        }
    }
}



@Preview(widthDp = 392, heightDp = 1271)
@Composable
private fun HeartRateScreenPreview() {
    HeartRateScreen()
}
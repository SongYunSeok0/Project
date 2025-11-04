package com.scheduler.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.common.design.R

@Composable
fun OcrScreen(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit = {},
    onRetake: () -> Unit = {},
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0),
        modifier = modifier
    ) { _: PaddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // 미리보기
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(486.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF3F4F6))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.upload),
                    contentDescription = "preview",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.fillMaxSize()
                )
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

            // 하단 버튼
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6AE0D9))
                ) {
                    Text("확인 및 등록", color = Color.White, fontSize = 16.sp, lineHeight = 1.5.em)
                }

                OutlinedButton(
                    onClick = onRetake,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF6AE0D9)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF6AE0D9)
                    )
                ) {
                    Text("다시 촬영", fontSize = 16.sp, lineHeight = 1.5.em)
                }
            }
        }
    }
}

@Preview(widthDp = 412, heightDp = 851, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun OcrScreenPreview() {
    OcrScreen()
}
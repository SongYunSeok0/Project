package com.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChartBar(label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // 바 형태
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(90.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xfff3f4f6)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((20..90).random().dp)    // 임시 높이 (랜덤)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xff6ae0d9), Color(0xff5bb8e8))
                        )
                    )
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(label, fontSize = 12.sp, color = Color(0xff4a5565))
    }
}
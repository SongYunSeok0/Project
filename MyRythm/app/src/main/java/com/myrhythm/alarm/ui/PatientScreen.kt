package com.myrhythm.alarm.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shared.R

@Composable
fun PatientScreen(
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB5E5E1))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🔼 위쪽 공간 (살짝만)
        Spacer(modifier = Modifier.height(100.dp))

        // 🔼 약 + 글씨 영역을 위쪽에 고정하려면 weight를 제거하고 패딩만 둬라
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.pill),
                contentDescription = null,
                modifier = Modifier.size(200.dp) // 크기를 키워도 전체가 내려가지 않음
            )

            Spacer(Modifier.height(20.dp))

            Text(
                "약 드실 시간이에요!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "복약 시간입니다",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(Modifier.height(100.dp))

        Button(
            onClick = onStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6B6B)
            )
        ) {
            Text("알람 끄기", fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PatientScreenPreview() {
    PatientScreen(onStop = {})
}

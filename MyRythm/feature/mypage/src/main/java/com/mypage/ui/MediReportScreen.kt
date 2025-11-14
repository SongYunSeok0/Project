package com.mypage.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.ui.components.AnalysisCard
import com.ui.components.MedicineItemCard
import com.ui.components.StatCard
import com.ui.components.TabText



@Composable
fun MediReportScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        // 탭
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabText(text = "복약 요약", selected = true)
            TabText(text = "개인 트래커", selected = false)
        }

        Spacer(Modifier.height(24.dp))

        // 상단 카드 3개
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard("85%", "이번 주 복약률", Color(0xff6ae0d9))
            StatCard("12일", "연속 복약 일수", Color(0xff59c606))
            StatCard("92%", "이번 달 준수율", Color(0xfff9c034))
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "복약별 준수율",
            fontSize = 16.sp,
            color = Color(0xff101828)
        )

        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MedicineItemCard("비타민 D", 95, "하루 1회 복용")
            MedicineItemCard("오메가3", 90, "하루 1회 복용")
            MedicineItemCard("종합비타민", 80, "하루 1회 복용")
        }

        Spacer(Modifier.height(32.dp))

        AnalysisCard(
            text = "이번 주는 지난 주보다 복약률이 10% 증가했습니다! 특히 아침 복약 시간을 잘 지키고 계시네요. 저녁 복약을 조금 더 신경 쓰시면 100% 달성할 수 있습니다."
        )
    }
}



@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
fun MediReportScreenPreview() {
    MediReportScreen()
}

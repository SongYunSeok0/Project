package com.main.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.ui.components.AddMedicineButton
import com.ui.components.MedicineScheduleItem
import com.ui.components.WeeklyChart

@Composable
fun AlarmScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        // 새 복약 추가 버튼
        AddMedicineButton()

        Spacer(Modifier.height(24.dp))

        Text(
            "오늘의 복약 일정",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xff101828)
        )

        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MedicineScheduleItem(
                title = "비타민 D",
                time = "08:00",
                dose = "1정",
                done = true
            )
            MedicineScheduleItem(
                title = "오메가3",
                time = "12:00",
                dose = "2정",
                done = true
            )
            MedicineScheduleItem(
                title = "종합비타민",
                time = "20:00",
                dose = "1정",
                done = false
            )
        }

        Spacer(Modifier.height(32.dp))

        WeeklyChart()
    }
}


@Preview(widthDp = 412, heightDp = 917)
@Composable
fun AlarmScreenPreview() {
    AlarmScreen()
}
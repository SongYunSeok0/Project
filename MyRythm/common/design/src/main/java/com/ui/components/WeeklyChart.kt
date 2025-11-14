package com.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun WeeklyChart() {

    Surface(
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xfff3f4f6)),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(Modifier.padding(20.dp)) {

            Text("주간 복약률", fontSize = 16.sp, color = Color(0xff101828))

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("월", "화", "수", "목", "금", "토", "일")
                days.forEach { day ->
                    ChartBar(label = day)
                }
            }
        }
    }
}
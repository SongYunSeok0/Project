package com.mypage.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domain.model.MediRecord
import com.shared.R
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun MediRecordCard(
    record: MediRecord,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // taken 상태 → UI 문구 + 색상
    val (statusText, statusColor) = when (record.taken) {
        true -> "복용 완료" to Color(0xFF16A34A)
        false -> "미복용" to Color(0xFFEF4444)
        null -> "미확인" to Color.Gray
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(
                border = BorderStroke(0.7.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {

        // ------------------------------------------------
        // 상단 Row : 약 아이콘 / 이름 / 라벨 / 상태칩 / 화살표
        // ------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.pill),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )

                Column {
                    // 약 이름
                    Text(
                        text = record.medicineName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF111827)
                    )

                    // 처방 라벨
                    record.regiLabel?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }

                    // 복용 시각
                    record.takenAt?.let {
                        Text(
                            text = formatTime(it),
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {

                // 상태칩
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Image(
                    painter = painterResource(if (expanded) R.drawable.arrow_up else R.drawable.arrow_down),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // ------------------------------------------------
        // Expanded 내용 (메모, mealTime 등)
        // ------------------------------------------------
        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "메모",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = record.memo ?: "메모 없음",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4B5563)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "복용 시간대: ${record.mealTime ?: "정보 없음"}",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}


// ------------------------------------------------
// 시간 변환: timestamp(ms) → "MM/dd HH:mm"
// ------------------------------------------------
private fun formatTime(timestamp: Long): String {
    return try {
        val odt = OffsetDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
        val formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")
        odt.format(formatter)
    } catch (e: Exception) {
        timestamp.toString()
    }
}

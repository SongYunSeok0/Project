package com.mypage.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domain.model.MediRecord
import com.shared.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// 그룹화된 복약 기록
data class GroupedMediRecord(
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val medNames: List<String>,
    val mealTime: String?,
    val memo: String?,
    val totalDoses: Int,
    val completedDoses: Int,
    val records: List<MediRecord>,
    val completionRate: Float
)

@Composable
fun GroupedMediRecordCard(
    group: GroupedMediRecord,
    modifier: Modifier = Modifier,
    onDeleteGroup: (GroupedMediRecord) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    val dateRange = "${group.startDate.format(dateFormatter)} - ${group.endDate.format(dateFormatter)}"

    val completionPercent = (group.completionRate * 100).toInt()

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

        // 상단: 라벨 / 날짜 범위 / 완료율 / 화살표
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateRange,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "완료율: ",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = "$completionPercent%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            completionPercent >= 80 -> Color(0xFF16A34A)
                            completionPercent >= 50 -> Color(0xFFF59E0B)
                            else -> Color(0xFFEF4444)
                        }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFFF3F4F6))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${group.records.size}회",
                        fontSize = 12.sp,
                        color = Color(0xFF374151)
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

        // Expanded: 처방 상세 정보
        if (expanded) {
            Spacer(modifier = Modifier.height(16.dp))

            // 약 이름들
            DetailInfoRow(
                label = "약 이름",
                content = {
                    Column {
                        group.medNames.forEach { medName ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.pill),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = medName,
                                    fontSize = 14.sp,
                                    color = Color(0xFF111827)
                                )
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailInfoRow(
                label = "복용 기간",
                value = dateRange
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailInfoRow(
                label = "복용 횟수",
                value = "${group.completedDoses}/${group.totalDoses}회"
            )

            Spacer(modifier = Modifier.height(12.dp))

            val mealTimeText = when (group.mealTime) {
                "before" -> "식전"
                "after" -> "식후"
                "none" -> "관계없음"
                else -> group.mealTime ?: "-"
            }
            DetailInfoRow(
                label = "식사 관계",
                value = mealTimeText
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailInfoRow(
                label = "메모",
                value = group.memo ?: "-"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 삭제 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFEF2F2))
                    .clickable { showDeleteDialog = true }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "기록 삭제하기",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFEF4444)
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            groupLabel = group.label,
            recordCount = group.records.size,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDeleteGroup(group)
            }
        )
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String? = null,
    content: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            modifier = Modifier.width(80.dp)
        )

        if (content != null) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        } else {
            Text(
                text = value ?: "-",
                fontSize = 14.sp,
                color = Color(0xFF111827),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    groupLabel: String,
    recordCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "기록 삭제",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF111827)
            )
        },
        text = {
            Column {
                Text(
                    text = "\"$groupLabel\" 처방의 모든 기록을 삭제하시겠습니까?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF374151)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "총 ${recordCount}개의 복약 기록이 삭제됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFEF4444)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "이 작업은 되돌릴 수 없습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("삭제", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF3F4F6)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("취소", color = Color(0xFF374151))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

// 복약 기록 그룹화 함수
fun groupMediRecords(records: List<MediRecord>): List<GroupedMediRecord> {
    val zone = ZoneId.systemDefault()

    return records
        .groupBy { it.regiLabel ?: "미분류" }
        .map { (label, groupRecords) ->
            val dates = groupRecords.mapNotNull { record ->
                record.takenAt?.let {
                    Instant.ofEpochMilli(it).atZone(zone).toLocalDate()
                }
            }

            val startDate = dates.minOrNull() ?: LocalDate.now()
            val endDate = dates.maxOrNull() ?: LocalDate.now()

            val medNames = groupRecords.map { it.medicineName }.distinct()

            val representative = groupRecords.first()

            val completedCount = groupRecords.count { it.taken == true }
            val totalCount = groupRecords.size
            val completionRate = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

            GroupedMediRecord(
                label = label,
                startDate = startDate,
                endDate = endDate,
                medNames = medNames,
                mealTime = representative.mealTime,
                memo = representative.memo,
                totalDoses = totalCount,
                completedDoses = completedCount,
                records = groupRecords.sortedByDescending { it.takenAt },
                completionRate = completionRate
            )
        }
        .sortedByDescending { it.endDate }
}
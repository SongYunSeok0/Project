package com.mypage.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domain.model.MediRecord
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.theme.AppTheme
import com.shared.ui.theme.componentTheme
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

    // 문자열 리소스
    val completionRateText = stringResource(R.string.completion_rate)
    val medicationNameText = stringResource(R.string.medication_name)
    val dosePeriodText = stringResource(R.string.dose_period)
    val mealRelationText = stringResource(R.string.meal_relation)
    val memoLabelText = stringResource(R.string.memo_label)
    val countSuffixText = stringResource(R.string.count_per_day)

    val recordDeleteButtonText = stringResource(R.string.record_delete_button)
    val doseCountText = stringResource(R.string.dose_count)
    val percentSuffixText = stringResource(R.string.percent_suffix)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                border = BorderStroke(
                    0.7.dp,
                    MaterialTheme.componentTheme.mainFeatureCardBorderStroke
                ),
                shape = MaterialTheme.shapes.large
            )
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
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
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = "$completionRateText: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Text(
                        text = "$completionPercent$percentSuffixText",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = when {
                            completionPercent >= 80 ->
                                MaterialTheme.componentTheme.heartRateNormal
                            completionPercent >= 50 ->
                                MaterialTheme.componentTheme.completionCaution
                            else ->
                                MaterialTheme.componentTheme.heartRateWarning
                        }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(MaterialTheme.componentTheme.mainFeatureCardBorderStroke)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${group.totalDoses}$countSuffixText",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Image(
                    painter = painterResource(
                        if (expanded) R.drawable.arrow_up else R.drawable.arrow_down
                    ),
                    contentDescription = stringResource(R.string.arrow_description),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Expanded: 처방 상세 정보
        if (expanded) {
            Spacer(modifier = Modifier.height(16.dp))

            // 약 이름들
            DetailInfoRow(
                label = medicationNameText,
                content = {
                    Column {
                        group.medNames.forEach { medName ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                Image(
                                    painter = painterResource(R.drawable.pill),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = medName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailInfoRow(
                label = dosePeriodText,
                value = dateRange
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailInfoRow(
                label = doseCountText,
                value = "${group.completedDoses}/${group.totalDoses}$countSuffixText"
            )

            Spacer(modifier = Modifier.height(12.dp))

            val mealTimeText = when (group.mealTime) {
                "before" -> stringResource(R.string.meal_relation_before)
                "after" -> stringResource(R.string.meal_relation_after)
                "none" -> stringResource(R.string.meal_relation_irrelevant)
                else -> group.mealTime ?: "-"
            }
            DetailInfoRow(
                label = mealRelationText,
                value = mealTimeText
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailInfoRow(
                label = memoLabelText,
                value = group.memo ?: "-"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 삭제 버튼

            AppButton(
                text = recordDeleteButtonText,
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                height = 48.dp,
                shape = MaterialTheme.shapes.large,
                textStyle = MaterialTheme.typography.labelMedium
            )
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
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.width(80.dp)
        )

        if (content != null) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        } else {
            Text(
                text = value ?: "-",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
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
    val questionMessage =
        stringResource(R.string.mypage_message_medi_record_delete_question, groupLabel)
    val countMessage =
        stringResource(R.string.mypage_message_medi_record_delete_count, recordCount)
    val warningMessage =
        stringResource(R.string.mypage_message_medi_record_delete_warning)

    val deleteText = stringResource(R.string.delete)
    val cancelText = stringResource(R.string.cancel)
    val deleteTitleText = stringResource(R.string.record_delete)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = deleteTitleText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Text(
                    text = questionMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = countMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.componentTheme.heartRateWarning
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = warningMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    deleteText,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape =MaterialTheme.shapes.small
            ) {
                Text(
                    cancelText,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape =MaterialTheme.shapes.extraLarge
    )
}

fun groupMediRecords(records: List<MediRecord>): List<GroupedMediRecord> {
    val zone = ZoneId.systemDefault()

    // 🔥 로그 추가
    android.util.Log.d("GroupMediRecords", "====== 전체 records: ${records.size}개 ======")
    records.forEach { record ->
        android.util.Log.d("GroupMediRecords",
            "Record: id=${record.id}, label=${record.regiLabel}, " +
                    "name=${record.medicineName}, taken=${record.taken}, takenAt=${record.takenAt}")
    }

    return records
        .groupBy { it.regiLabel ?: "미분류" }
        .map { (label, groupRecords) ->
            // 🔥 로그 추가
            android.util.Log.d("GroupMediRecords", "====== Label: $label (${groupRecords.size}개) ======")

            val dates = groupRecords.mapNotNull { record ->
                record.takenAt?.let {
                    Instant.ofEpochMilli(it).atZone(zone).toLocalDate()
                }
            }

            val startDate = dates.minOrNull() ?: LocalDate.now()
            val endDate = dates.maxOrNull() ?: LocalDate.now()

            val medNames = groupRecords.map { it.medicineName }.distinct()

            val representative = groupRecords.first()

            // 시간대별 그룹화
            val timeSlots = groupRecords.groupBy { it.takenAt }

            // 🔥 로그 추가
            android.util.Log.d("GroupMediRecords", "총 ${timeSlots.size}개 시간대")
            timeSlots.forEach { (time, recordsAtSameTime) ->
                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                val timeStr = if (time != null) formatter.format(java.util.Date(time)) else "null"
                val allTaken = recordsAtSameTime.all { it.taken == true }
                android.util.Log.d("GroupMediRecords",
                    "  시간: $timeStr, 약 ${recordsAtSameTime.size}개, 모두 복용: $allTaken")
                recordsAtSameTime.forEach { r ->
                    android.util.Log.d("GroupMediRecords",
                        "    - ${r.medicineName}: taken=${r.taken}")
                }
            }

            val completedTimeSlots = timeSlots.count { (_, recordsAtSameTime) ->
                recordsAtSameTime.all { it.taken == true }
            }

            val totalTimeSlots = timeSlots.size
            val completionRate = if (totalTimeSlots > 0)
                completedTimeSlots.toFloat() / totalTimeSlots
            else 0f

            // 🔥 로그 추가
            android.util.Log.d("GroupMediRecords",
                "결과 - 완료: $completedTimeSlots/$totalTimeSlots, 완료율: ${(completionRate * 100).toInt()}%")

            GroupedMediRecord(
                label = label,
                startDate = startDate,
                endDate = endDate,
                medNames = medNames,
                mealTime = representative.mealTime,
                memo = representative.memo,
                totalDoses = totalTimeSlots,
                completedDoses = completedTimeSlots,
                records = groupRecords.sortedByDescending { it.takenAt },
                completionRate = completionRate
            )
        }
        .sortedByDescending { it.endDate }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewGroupedMediRecordCard() {
    AppTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            GroupedMediRecordCard(
                group = sampleGroupedMediRecord(),
                onDeleteGroup = {}
            )
        }
    }
}

@Composable
fun sampleGroupedMediRecord(): GroupedMediRecord {
    val today = LocalDate.now()

    return GroupedMediRecord(
        label = "고지혈증 처방",
        startDate = today.minusDays(7),
        endDate = today,
        medNames = listOf("로수바스타틴 10mg", "아스피린 100mg"),
        mealTime = "after",
        memo = "식후에 바로 복용하세요.",
        totalDoses = 14,
        completedDoses = 9,
        records = emptyList(),
        completionRate = 9f / 14f
    )
}

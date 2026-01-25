package com.scheduler.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.scheduler.ui.IntakeStatus
import com.scheduler.ui.MedItem
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.theme.AppFieldHeight

@Composable
fun MedDetailDialog(
    item: MedItem,
    onDismiss: () -> Unit,
    onToggleAlarm: (Boolean) -> Unit,
    isDeviceUser: Boolean = false,
    onMarkTaken: (() -> Unit)? = null       // 복용 완료 처리 콜백
) {
    val detailTitle = stringResource(R.string.detail_title)
    val regiLabel = stringResource(R.string.regi_label)
    val medNameLabel = stringResource(R.string.med_name_label)
    val mealTimeLabel = stringResource(R.string.meal_relation)
    val noMemoLabel = stringResource(R.string.no_memo)
    val memoLabel = stringResource(R.string.memo_label)
    val alarmLabel = stringResource(R.string.alarm_label)
    val closeText = stringResource(R.string.close)

    val mealTimeText = when (item.mealTime) {
        "before" -> stringResource(R.string.meal_relation_before)
        "after" -> stringResource(R.string.meal_relation_after)
        "none" -> stringResource(R.string.meal_relation_irrelevant)
        else -> "-"
    }

    // 🔒 알림 스위치: SCHEDULED 상태에서만 켤/끌 수 있음
    //  - 기기 연동 사용자(isDeviceUser)는 항상 비활성화
    val alarmSwitchEnabled =
        !isDeviceUser && item.status == IntakeStatus.SCHEDULED

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    detailTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                DetailRow(regiLabel, item.label)
                DetailMedNames(medNameLabel, item.medNames)
                DetailRow(mealTimeLabel, mealTimeText)
                DetailRow(memoLabel, item.memo ?: noMemoLabel)

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        alarmLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Switch(
                        checked = item.useAlarm,
                        enabled = alarmSwitchEnabled,
                        onCheckedChange = {
                            if (alarmSwitchEnabled) {
                                onToggleAlarm(it)
                            }
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))

                if (item.status == IntakeStatus.MISSED ||
                    item.status == IntakeStatus.SCHEDULED) {
                    // 🔹 미복용 / 예정 일정: 복용 완료 + 닫기 버튼 나란히
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppButton(
                            text = "복용 완료",
                            modifier = Modifier
                                .weight(1f)
                                .height(AppFieldHeight),
                            shape = MaterialTheme.shapes.medium,
                            onClick = {
                                onMarkTaken?.invoke()
                            }
                        )
                        AppButton(
                            text = closeText,
                            modifier = Modifier
                                .weight(1f)
                                .height(AppFieldHeight),
                            shape = MaterialTheme.shapes.medium,
                            onClick = onDismiss
                        )
                    }
                } else {
                    // 🔹 DONE: 닫기만 전체 폭
                    AppButton(
                        text = closeText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppFieldHeight),
                        shape = MaterialTheme.shapes.medium,
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DetailMedNames(label: String, medNames: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.width(80.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (medNames.isEmpty() || medNames.all { it.isBlank() }) {
                Text(
                    "-",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                medNames.forEach { name ->
                    Text(
                        text = "• $name",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}
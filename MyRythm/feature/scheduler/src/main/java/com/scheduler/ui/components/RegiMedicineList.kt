package com.scheduler.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.* // remember, mutableStateOf 등을 위해 필요
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scheduler.ui.RegiController
import com.scheduler.ui.RegiTab
import com.scheduler.ui.WheelTimePickerDialog
import com.shared.R
import com.shared.ui.components.AppInputField
import com.shared.ui.theme.AppFieldHeight

@Composable
fun RegiMedicineListSection(controller: RegiController) {
    val medicationNameText = stringResource(R.string.medication_name)
    val enterMedicationNameMessage = stringResource(R.string.scheduler_message_medication_name)
    val doseDailyCount = stringResource(R.string.dose_daily_count)
    val countPerDayText = stringResource(R.string.count_per_day)
    val doseTime = stringResource(R.string.dose_time)

    if (controller.tab == RegiTab.DISEASE) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(medicationNameText, color = MaterialTheme.colorScheme.onSurface)

            Spacer(Modifier.height(4.dp))

            controller.meds.forEachIndexed { idx, value ->
                AppInputField(
                    value = value,
                    onValueChange = { controller.meds[idx] = it },
                    label = enterMedicationNameMessage,
                    singleLine = true,
                    trailingContent = {
                        if (idx == controller.meds.lastIndex)
                            IconButton(onClick = { controller.meds.add("") }) {
                                Icon(Icons.Default.Add, null)
                            }
                        else if (controller.meds.size > 1)
                            IconButton(onClick = { controller.meds.removeAt(idx) }) {
                                Icon(Icons.Default.Close, null)
                            }
                    }
                )
            }
        }
    }

    Spacer(Modifier.height(4.dp))

    // 복용 횟수
    Column {
        Text(doseDailyCount, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary.copy(0.1f))
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    controller.onDoseChange(controller.dose - 1)
                }
            ) {
                Icon(Icons.Default.Remove, null)
            }

            Spacer(Modifier.weight(1f))

            Text(
                "${controller.dose}$countPerDayText",
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = {
                    controller.onDoseChange(controller.dose + 1)
                }
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // 복용 시간
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(doseTime, color = MaterialTheme.colorScheme.onSurface)
        controller.intakeTimes.forEachIndexed { i, t ->
            TimeInputRow("${i + 1}$countPerDayText", t) { new -> controller.intakeTimes[i] = new }
        }
    }
    Spacer(Modifier.height(4.dp))
}

/* 시간 입력 Row */
@Composable
fun TimeInputRow(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    val timeExampleText = stringResource(R.string.time_example)

    var showPicker by remember { mutableStateOf(false) }

    val initialHour = value.split(":").getOrNull(0)?.toIntOrNull() ?: 12
    val initialMin = value.split(":").getOrNull(1)?.toIntOrNull() ?: 0

    if (showPicker) {
        WheelTimePickerDialog(
            hour = initialHour,
            minute = initialMin,
            onDismiss = { showPicker = false },
            onConfirm = {
                onChange(it)
                showPicker = false
            }
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppFieldHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight()
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(MaterialTheme.shapes.large)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.large
                )
                .background(MaterialTheme.colorScheme.background)
                .clickable { showPicker = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (value.isBlank()) timeExampleText else value,
                color = if (value.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
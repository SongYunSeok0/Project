package com.scheduler.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.domain.model.Device
import com.scheduler.ui.RegiController
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.components.AppInputField
import com.shared.ui.theme.AppFieldHeight

@Composable
fun RegiFooterSection(
    controller: RegiController,
    devices: List<Device>
) {
    val memoNotesText = stringResource(R.string.memo_notes)
    val enterMemoMessage = stringResource(R.string.scheduler_message_enter_memo)
    val iotDeviceText = stringResource(R.string.iotdevice)
    val alarmSettingText = stringResource(R.string.alarm_setting)
    val alarmOnText = stringResource(R.string.isalarm_on)
    val alarmOffText = stringResource(R.string.isalarm_off)
    val registrationComplete = stringResource(R.string.registration_complete)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // 메모
        Column {
            Text(memoNotesText, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            AppInputField(
                value = controller.memo,
                onValueChange = { controller.memo = it },
                label = enterMemoMessage,
                maxLines = 3
            )
        }

        // IoT 기기 선택 드롭다운
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(iotDeviceText, color = MaterialTheme.colorScheme.background)
            DeviceDropdown(
                devices = devices,
                selectedDevice = controller.selectedDevice,
                onSelectedChange = { controller.selectedDevice = it }
            )
        }

        // 알람
        Column {
            Text(alarmSettingText, color = MaterialTheme.colorScheme.onSurface)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    if (controller.useAlarm) alarmOnText else alarmOffText,
                    style = MaterialTheme.typography.bodySmall
                )
                Switch(checked = controller.useAlarm, onCheckedChange = { controller.useAlarm = it })
            }
        }

        // 등록 버튼
        AppButton(
            text = registrationComplete,
            modifier = Modifier.fillMaxWidth().height(AppFieldHeight),
            onClick = { controller.submit() }
        )
    }
}

/* IoT 기기 선택 드롭다운 (도메인 Device 사용) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceDropdown(
    devices: List<Device>,
    selectedDevice: Long?,
    onSelectedChange: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedLabel = remember(devices, selectedDevice) {
        devices.firstOrNull { it.id == selectedDevice }?.name
    }

    val label = when {
        devices.isEmpty() -> "연결된 기기 없음"
        selectedLabel != null -> selectedLabel
        else -> "기기를 선택하세요"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (devices.isNotEmpty()) {
                expanded = !expanded
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            enabled = devices.isNotEmpty(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )

        if (devices.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("연동하지 않음") },
                    onClick = {
                        onSelectedChange(null)
                        expanded = false
                    }
                )

                devices.forEach { device ->
                    DropdownMenuItem(
                        text = { Text(device.name) },
                        onClick = {
                            onSelectedChange(device.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
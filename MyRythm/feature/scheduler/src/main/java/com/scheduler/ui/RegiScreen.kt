@file:Suppress("UnusedImport")

package com.scheduler.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shared.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.domain.model.Device
import com.domain.model.Plan
import com.scheduler.viewmodel.RegiViewModel
import com.shared.ui.components.AppButton
import com.shared.ui.components.AppInputField
import com.shared.ui.components.AppSelectableButton
import com.shared.ui.theme.AppFieldHeight
import com.shared.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

private enum class RegiTab { DISEASE, SUPPLEMENT }

private fun presetTimes(n: Int): List<String> = when (n) {
    1 -> listOf("08:00")
    2 -> listOf("08:00", "18:00")
    3 -> listOf("08:00", "12:00", "18:00")
    4 -> listOf("08:00", "12:00", "18:00", "22:00")
    else -> List(n) { "" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegiScreen(
    modifier: Modifier = Modifier,
    drugNames: List<String> = emptyList(),
    times: Int? = null,
    days: Int? = null,
    viewModel: RegiViewModel = hiltViewModel(),
    onCompleted: () -> Unit = {},
    regihistoryId: Long? = null,
) {
    val diseaseText = stringResource(R.string.disease)
    val supplementText = stringResource(R.string.supplement)
    val diseaseNameText = stringResource(R.string.disease_name)
    val supplementNameText = stringResource(R.string.supplement_name)
    val medicationNameText = stringResource(R.string.medication_name)
    val doseDailyCount = stringResource(R.string.dose_daily_count)
    val doseTime = stringResource(R.string.dose_time)
    val dosePeriod = stringResource(R.string.dose_period)
    val startDateText = stringResource(R.string.start_date)
    val endDateText = stringResource(R.string.end_date)
    val mealRelationText = stringResource(R.string.meal_relation)
    val mealRelationBefore = stringResource(R.string.meal_relation_before)
    val mealRelationAfter = stringResource(R.string.meal_relation_after)
    val mealRelationIrrelevant = stringResource(R.string.meal_relation_irrelevant)
    val memoNotesText = stringResource(R.string.memo_notes)
    val registrationComplete = stringResource(R.string.registration_complete)
    val cancelText = stringResource(R.string.cancel)
    val confirmText = stringResource(R.string.confirm)
    val alarmSettingText = stringResource(R.string.alarm_setting)
    val alarmOnText = stringResource(R.string.isalarm_on)
    val alarmOffText = stringResource(R.string.isalarm_off)
    val countPerDayText = stringResource(R.string.count_per_day)
    val registrationSuccessMessage = stringResource(R.string.scheduler_message_registration_success)
    val registrationFailedMessage = stringResource(R.string.scheduler_message_registration_failed)
    val enterDiseaseNameMessage = stringResource(R.string.scheduler_message_disease_name)
    val enterSupplementNameMessage = stringResource(R.string.scheduler_message_supplement_name)
    val enterMedicationNameMessage = stringResource(R.string.scheduler_message_medication_name)
    val enterMemoMessage = stringResource(R.string.scheduler_message_enter_memo)

    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { msg ->
            when (msg) {
                "등록 완료" -> {
                    Toast.makeText(context, registrationSuccessMessage, Toast.LENGTH_SHORT).show()
                    onCompleted()
                }

                "등록 실패" ->
                    Toast.makeText(context, registrationFailedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(regihistoryId) {
        viewModel.initRegi(regihistoryId)
    }

    // IoT 기기 로드
    LaunchedEffect(Unit) {
        viewModel.loadMyDevices()
    }

    // ViewModel에서 도메인 Device 리스트 받기
    val devices by viewModel.devices.collectAsState()
    var selectedDevice by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(regihistoryId) {
        viewModel.initRegi(regihistoryId)
    }

    var tab by remember { mutableStateOf(RegiTab.DISEASE) }

    var disease by remember { mutableStateOf("") }
    var supplement by remember { mutableStateOf("") }

    val meds = remember {
        mutableStateListOf<String>().apply {
            if (drugNames.isNotEmpty()) addAll(drugNames) else add("")
        }
    }

    var initialized by remember { mutableStateOf(false) }

    var dose by remember { mutableIntStateOf(3) }
    val intakeTimes = remember { mutableStateListOf<String>() }

    var mealRelation by remember { mutableStateOf("after") }
    var memo by remember { mutableStateOf("") }
    var useAlarm by remember { mutableStateOf(true) }

    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    fun todayStr() = dateFmt.format(Calendar.getInstance().time)
    fun strToMillis(s: String): Long? = runCatching { dateFmt.parse(s)?.time }.getOrNull()

    var startDay by remember { mutableStateOf("") }
    var endDay by remember { mutableStateOf("") }

    var showStart by remember { mutableStateOf(false) }
    var showEnd by remember { mutableStateOf(false) }

    // 최초 1회 초기화
    LaunchedEffect(Unit) {
        if (!initialized) {
            initialized = true
            dose = times ?: 3
            intakeTimes.clear()
            if (drugNames.isNotEmpty()) {
                tab = RegiTab.DISEASE
                intakeTimes.addAll(presetTimes(dose))
            } else {
                intakeTimes.addAll(presetTimes(3))
            }
            startDay = todayStr()
            if (days != null) {
                val end = Calendar.getInstance()
                end.add(Calendar.DAY_OF_YEAR, days - 1)
                endDay = dateFmt.format(end.time)
            }
        }
    }

    LaunchedEffect(tab) {
        if (!initialized) return@LaunchedEffect
        intakeTimes.clear()
        if (tab == RegiTab.SUPPLEMENT) {
            dose = 1
            intakeTimes.add("12:00")
        } else {
            dose = 3
            intakeTimes.addAll(presetTimes(3))
        }
    }

    LaunchedEffect(dose) {
        if (intakeTimes.size < dose) {
            repeat(dose - intakeTimes.size) { intakeTimes.add("") }
        } else if (intakeTimes.size > dose) {
            repeat(intakeTimes.size - dose) {
                intakeTimes.removeAt(intakeTimes.lastIndex)
            }
        }
    }

    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { inner ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 탭
            TabRow(
                selectedTabIndex = if (tab == RegiTab.DISEASE) 0 else 1,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                indicator = { pos ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(pos[if (tab == RegiTab.DISEASE) 0 else 1]),
                        height = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = tab == RegiTab.DISEASE,
                    onClick = { tab = RegiTab.DISEASE },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.surfaceVariant,
                    text = { Text(diseaseText) }
                )
                Tab(
                    selected = tab == RegiTab.SUPPLEMENT,
                    onClick = { tab = RegiTab.SUPPLEMENT },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.surfaceVariant,
                    text = { Text(supplementText) }
                )
            }

            // 병명 / 영양제
            if (tab == RegiTab.DISEASE) {
                Column {
                    Text(diseaseNameText, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    AppInputField(
                        value = disease,
                        onValueChange = { disease = it },
                        label = enterDiseaseNameMessage,
                        singleLine = true,
                    )
                }
            } else {
                Column {
                    Text(supplementNameText, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    AppInputField(
                        value = supplement,
                        onValueChange = { supplement = it },
                        label = enterSupplementNameMessage,
                        singleLine = true,
                    )
                }
            }

            // 약 이름 리스트
            if (tab == RegiTab.DISEASE) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(medicationNameText, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))
                    meds.forEachIndexed { idx, value ->
                        AppInputField(
                            value = value,
                            onValueChange = { meds[idx] = it },
                            label = enterMedicationNameMessage,
                            singleLine = true,
                            trailingContent = {
                                if (idx == meds.lastIndex)
                                    IconButton(onClick = { meds.add("") }) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                    }
                                else if (meds.size > 1)
                                    IconButton(onClick = { meds.removeAt(idx) }) {
                                        Icon(Icons.Default.Close, contentDescription = null)
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
                    IconButton(onClick = {
                        dose = (dose - 1).coerceAtLeast(1)
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = null)
                    }

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = "${dose}$countPerDayText",
                        style = MaterialTheme.typography.labelLarge,
                    )

                    Spacer(Modifier.weight(1f))

                    IconButton(onClick = {
                        dose = (dose + 1).coerceAtMost(6)
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // 복용 시간
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(doseTime, color = MaterialTheme.colorScheme.onSurface)
                intakeTimes.forEachIndexed { i, t ->
                    TimeInputRow("${i + 1}$countPerDayText", t) { new -> intakeTimes[i] = new }
                }
            }
            Spacer(Modifier.height(4.dp))
            // 기간
            Column {
                Text(dosePeriod, color = MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateBox(startDateText, startDay, Modifier.weight(1f)) {
                        showStart = true
                    }
                    DateBox(endDateText, endDay, Modifier.weight(1f)) {
                        showEnd = true
                    }
                }
            }

            // 식사 관계
            if (tab == RegiTab.DISEASE) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(mealRelationText, color = MaterialTheme.colorScheme.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppSelectableButton(
                            text = mealRelationBefore,
                            selected = mealRelation == "before",
                            onClick = { mealRelation = "before" },
                            modifier = Modifier.weight(1f),
                            height = 44.dp
                        )

                        AppSelectableButton(
                            text = mealRelationAfter,
                            selected = mealRelation == "after",
                            onClick = { mealRelation = "after" },
                            modifier = Modifier.weight(1f),
                            height = 44.dp
                        )

                        AppSelectableButton(
                            text = mealRelationIrrelevant,
                            selected = mealRelation == "none",
                            onClick = { mealRelation = "none" },
                            modifier = Modifier.weight(1f),
                            height = 44.dp
                        )
                    }
                }
            }

            // 메모
            Column {
                Text(memoNotesText, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                AppInputField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = enterMemoMessage,
                    maxLines = 3
                )
            }

            // IoT 기기 선택 드롭다운
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("연동할 IoT 기기", color = MaterialTheme.colorScheme.onSurface)

                DeviceDropdown(
                    devices = devices,
                    selectedDevice = selectedDevice,
                    onSelectedChange = { selectedDevice = it }
                )
            }

            // 알람
            Column {
                Text(
                    alarmSettingText,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        if (useAlarm) alarmOnText else alarmOffText,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Switch(checked = useAlarm, onCheckedChange = { useAlarm = it })
                }
            }

            // 등록 버튼
            AppButton(
                text = registrationComplete,
                textStyle = MaterialTheme.typography.bodyLarge,
                onClick = {

                    val regiType = if (tab == RegiTab.DISEASE) "disease" else "supplement"
                    val label =
                        if (tab == RegiTab.DISEASE) disease.ifBlank { null }
                        else supplement.ifBlank { null }

                    val sMs = strToMillis(startDay) ?: System.currentTimeMillis()
                    var eMs = strToMillis(endDay) ?: sMs
                    val oneDay = 86400000L

                    val dfDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

                    val realMeds =
                        if (tab == RegiTab.SUPPLEMENT)
                            listOfNotNull(supplement.ifBlank { null })
                        else meds.mapNotNull { it.ifBlank { null } }

                    val realTimes = intakeTimes.mapNotNull { it.ifBlank { null } }

                    val nowMs = System.currentTimeMillis()

                    run {
                        val lastDayStr = dfDay.format(Date(eMs))
                        val allTimes = realTimes.map { t ->
                            LocalDateTime.parse("$lastDayStr $t", formatter)
                                .atZone(ZoneId.of("Asia/Seoul"))
                                .toInstant()
                                .toEpochMilli()
                        }
                        if (allTimes.all { it < nowMs }) {
                            eMs += oneDay
                            endDay = dfDay.format(Date(eMs))
                        }
                    }

                    val dayList = buildList {
                        var cur = sMs
                        while (cur <= eMs) {
                            add(cur)
                            cur += oneDay
                        }
                    }

                    val plans = mutableListOf<Plan>()

                    dayList.forEachIndexed { index, d ->
                        val ds = dfDay.format(Date(d))
                        realTimes.forEach { t ->
                            val date = LocalDate.parse(ds)
                            val time = LocalTime.parse(t)

                            val base = ZonedDateTime.of(date, time, ZoneId.of("Asia/Seoul"))
                                .toInstant()
                                .toEpochMilli()

                            // 첫날의 지난 시간은 종료일 다음날로 미룸
                            val takenAt = if (index == 0 && base < nowMs) {
                                base + (eMs - sMs) + oneDay
                            } else {
                                base
                            }

                            realMeds.forEach { med ->
                                plans += Plan(
                                    id = 0L,
                                    regihistoryId = regihistoryId,
                                    medName = med,
                                    takenAt = takenAt,
                                    mealTime = mealRelation,
                                    note = memo.ifBlank { null },
                                    taken = null,
                                    exTakenAt = takenAt,
                                    useAlarm = useAlarm
                                )
                            }
                        }
                    }

                    viewModel.createRegiAndPlans(
                        regiType = regiType,
                        label = label,
                        issuedDate = startDay,
                        useAlarm = useAlarm,
                        plans = plans,
                        device = selectedDevice
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppFieldHeight),
                shape = MaterialTheme.shapes.medium
            )
            Spacer(Modifier.height(20.dp))
        }
    }

    if (showStart) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = strToMillis(startDay) ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStart = false },
            confirmButton = {
                AppButton(
                    text = confirmText,
                    height = 40.dp,
                    width = 70.dp,
                    onClick = {
                        state.selectedDateMillis?.let { startDay = dateFmt.format(Date(it)) }
                        showStart = false
                    }
                )
            },
            dismissButton = {
                AppButton(
                    text = cancelText,
                    height = 40.dp,
                    width = 70.dp,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { showStart = false }
                )
            }
        ) { DatePicker(state = state) }
    }

    if (showEnd) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = strToMillis(endDay) ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEnd = false },
            confirmButton = {
                AppButton(
                    text = confirmText,
                    height = 40.dp,
                    width = 70.dp,
                    onClick = {
                        state.selectedDateMillis?.let { endDay = dateFmt.format(Date(it)) }
                        showEnd = false
                    }
                )
            },
            dismissButton = {
                AppButton(
                    text = cancelText,
                    height = 40.dp,
                    width = 70.dp,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
                    textColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { showEnd = false }
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                DatePicker(state = state)
            }
        }
    }
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

/* 날짜 박스 */
@Composable
private fun DateBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val selectText = stringResource(R.string.select)

    Column(modifier = modifier) {
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppFieldHeight)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.primary.copy(0.1f))
                .clickable { onClick() },
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value.ifBlank { selectText },
                color = if (value.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(14.dp)
            )
        }
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

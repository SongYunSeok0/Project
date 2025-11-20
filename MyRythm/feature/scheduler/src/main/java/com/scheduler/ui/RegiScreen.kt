package com.scheduler.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.shared.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.scheduler.viewmodel.PlanViewModel
import java.text.SimpleDateFormat
import java.util.*

private val CardBg = Color(0xFFF9FAFB)
private val SectionTitle = Color(0xFF3B566E)
private val Hint = Color(0x800A0A0A)

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
    userId: Long,
    prescriptionId: Long,
    modifier: Modifier = Modifier,
    drugNames: List<String> = emptyList(),
    times: Int? = null,
    days: Int? = null,
    viewModel: PlanViewModel = hiltViewModel(),
    onCompleted: () -> Unit = {},
) {
    val addiconDescription = stringResource(R.string.addicon_description)
    val removeiconDescription = stringResource(R.string.removeicon_description)
    val minusiconDescription = stringResource(R.string.minusicon_description)
    val plusiconDescription = stringResource(R.string.plusicon_description)
    val daySuffixText = stringResource(R.string.day_suffix)
    val cancelText = stringResource(R.string.cancel)
    val confirmText = stringResource(R.string.confirm)
    val diseaseText = stringResource(R.string.disease)
    val supplementText = stringResource(R.string.supplement)
    val diseaseNameText = stringResource(R.string.disease_name)
    val supplementNameText = stringResource(R.string.supplement_name)
    val medicationNameText = stringResource(R.string.medication_name)
    val doseDailyCount = stringResource(R.string.dose_daily_count)
    val countPerDay = stringResource(R.string.count_per_day)
    val doseTime = stringResource(R.string.dose_time)
    val timeSlot = stringResource(R.string.time_slot)
    val dosePeriod = stringResource(R.string.dose_period)
    val startDateText = stringResource(R.string.start_date)
    val endDateText = stringResource(R.string.end_date)
    val mealRelationText = stringResource(R.string.meal_relation)
    val mealRelationBefore = stringResource(R.string.meal_relation_before)
    val mealRelationAfter = stringResource(R.string.meal_relation_after)
    val mealRelationIrrelevant = stringResource(R.string.meal_relation_irrelevant)
    val memoNotesText = stringResource(R.string.memo_notes)
    val registrationComplete = stringResource(R.string.registration_complete)
    val diseaseNameMessage = stringResource(R.string.scheduler_message_disease_name)
    val supplementNameMessage = stringResource(R.string.scheduler_message_supplement_name)
    val medicationNameMessage = stringResource(R.string.scheduler_message_medication_name)
    val doseNotesMessage = stringResource(R.string.scheduler_message_dose_notes)

    var tab by remember { mutableStateOf(RegiTab.DISEASE) }

    // 질병/영양제 이름
    var disease by remember { mutableStateOf("") }
    var supplement by remember { mutableStateOf("") }

    // 약 이름 리스트
    val meds = remember {
        mutableStateListOf<String>().apply {
            if (drugNames.isNotEmpty()) addAll(drugNames)
            else add("")
        }
    }

    // 하루 복용 횟수
    var dose by remember { mutableIntStateOf(3) }

    // 식사 관계 (String)
    var mealRelation by remember { mutableStateOf("after") } // before / after / none

    var memo by remember { mutableStateOf("") }

    // 시간 리스트
    val intakeTimes = remember { mutableStateListOf<String>() }

    // 날짜 포맷터
    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val dateTimeFmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    fun todayStr() = dateFmt.format(Calendar.getInstance().time)
    fun strToMillis(s: String): Long? = runCatching { dateFmt.parse(s)?.time }.getOrNull()

    var startDay by remember { mutableStateOf("") }
    var endDay by remember { mutableStateOf("") }

    // 총 일수 계산
    val totalDaysLabel by remember(startDay, endDay) {
        mutableStateOf(
            run {
                val s = strToMillis(startDay)
                val e = strToMillis(endDay)
                if (s != null && e != null && e >= s) {
                    val daysInclusive =
                        ((e - s) / (1000L * 60 * 60 * 24)).toInt() + 1
                    "(${daysInclusive}$daySuffixText)"
                } else "($daySuffixText)"
            }
        )
    }

    // 달력 다이얼로그
    var showStart by remember { mutableStateOf(false) }
    var showEnd by remember { mutableStateOf(false) }

    if (showStart) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = strToMillis(startDay) ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStart = false },
            confirmButton = {
                Button(
                    onClick = {
                        state.selectedDateMillis?.let { startDay = dateFmt.format(Date(it)) }
                        showStart = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text(confirmText, color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showStart = false }) { Text(cancelText) }
            },
        ) { DatePicker(state = state) }
    }

    if (showEnd) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = strToMillis(endDay) ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEnd = false },
            confirmButton = {
                Button(
                    onClick = {
                        state.selectedDateMillis?.let { endDay = dateFmt.format(Date(it)) }
                        showEnd = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text(confirmText, color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEnd = false }) { Text(cancelText) }
            },
        ) { DatePicker(state = state) }
    }

    // 초기 세팅
    LaunchedEffect(Unit) {
        dose = 3
        intakeTimes.clear()
        intakeTimes.addAll(presetTimes(3))
        startDay = todayStr()

        endDay = days?.let {
            val c2 = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, it.coerceAtLeast(1) - 1)
            }
            dateFmt.format(c2.time)
        } ?: ""

        times?.let {
            dose = it.coerceIn(1, 6)
            intakeTimes.clear()
            intakeTimes.addAll(presetTimes(dose))
        }
    }

    // 탭 변경
    LaunchedEffect(tab) {
        if (tab == RegiTab.SUPPLEMENT) {
            dose = 1
            intakeTimes.clear()
            intakeTimes.add("12:00")
            startDay = todayStr()
            endDay = ""
        } else {
            dose = 3
            intakeTimes.clear()
            intakeTimes.addAll(presetTimes(3))
        }
    }

    // 횟수 변경
    LaunchedEffect(dose, tab) {
        intakeTimes.clear()
        if (tab == RegiTab.SUPPLEMENT) {
            if (dose == 1) intakeTimes.add("12:00")
            else repeat(dose) { intakeTimes.add("") }
        } else {
            intakeTimes.addAll(presetTimes(dose))
        }
    }

    // UI =============================================================
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
                containerColor = Color.Transparent,
                indicator = { positions ->
                    val idx = if (tab == RegiTab.DISEASE) 0 else 1
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(positions[idx]),
                        height = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = tab == RegiTab.DISEASE,
                    onClick = { tab = RegiTab.DISEASE },
                    text = { Text(diseaseText, color = if (tab == RegiTab.DISEASE) MaterialTheme.colorScheme.primary else SectionTitle) }
                )
                Tab(
                    selected = tab == RegiTab.SUPPLEMENT,
                    onClick = { tab = RegiTab.SUPPLEMENT },
                    text = { Text(supplementText, color = if (tab == RegiTab.SUPPLEMENT) MaterialTheme.colorScheme.primary else SectionTitle) }
                )
            }

            // 메인 입력
            when (tab) {
                RegiTab.DISEASE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(diseaseNameText, color = SectionTitle, fontSize = 14.sp)
                        OutlinedTextField(
                            value = disease,
                            onValueChange = { disease = it },
                            placeholder = { Text(diseaseNameMessage, color = Hint) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                RegiTab.SUPPLEMENT -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(supplementNameText, color = SectionTitle, fontSize = 14.sp)
                        OutlinedTextField(
                            value = supplement,
                            onValueChange = { supplement = it },
                            placeholder = { Text(supplementNameMessage, color = Hint) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 약 이름: 질병에서만
            if (tab == RegiTab.DISEASE) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(medicationNameText, color = SectionTitle)
                    meds.forEachIndexed { idx, value ->
                        val isLast = idx == meds.lastIndex

                        OutlinedTextField(
                            value = value,
                            onValueChange = { meds[idx] = it },
                            placeholder = { Text(medicationNameMessage, color = Hint) },
                            singleLine = true,
                            trailingIcon = {
                                if (isLast) {
                                    IconButton(onClick = { meds.add("") }) {
                                        Icon(Icons.Filled.Add, contentDescription = addiconDescription, tint = MaterialTheme.colorScheme.primary)
                                    }
                                } else if (meds.size > 1) {
                                    IconButton(onClick = { meds.removeAt(idx) }) {
                                        Icon(Icons.Filled.Close, contentDescription = removeiconDescription)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 횟수
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(doseDailyCount, color = SectionTitle)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { dose = (dose - 1).coerceAtLeast(1) }) {
                        Icon(Icons.Filled.Remove, contentDescription = minusiconDescription, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("${dose}$countPerDay", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { dose = (dose + 1).coerceAtMost(6) }) {
                        Icon(Icons.Filled.Add, contentDescription = plusiconDescription, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // 시간
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(doseTime, color = SectionTitle)
                intakeTimes.forEachIndexed { i, t ->
                    RepeatTimeRow("${i + 1}$timeSlot", t) { new ->
                        intakeTimes[i] = new
                    }
                }
            }

            // 기간
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dosePeriod, color = SectionTitle)
                    Text(totalDaysLabel, color = Color(0xFF6F8BA4))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateBox(startDateText, startDay, Modifier.weight(1f)) { showStart = true }
                    DateBox(endDateText, endDay, Modifier.weight(1f)) { showEnd = true }
                }
            }

            // 식사 관계 (질병 탭만)
            if (tab == RegiTab.DISEASE) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(mealRelationText, color = SectionTitle)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        SegChip(mealRelationBefore, selected = mealRelation == "before", modifier = Modifier.weight(1f)) {
                            mealRelation = "before"
                        }

                        SegChip(mealRelationAfter, selected = mealRelation == "after", modifier = Modifier.weight(1f)) {
                            mealRelation = "after"
                        }

                        SegChip(mealRelationIrrelevant, selected = mealRelation == "none", modifier = Modifier.weight(1f)) {
                            mealRelation = "none"
                        }
                    }
                }
            }

            // 메모
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(memoNotesText, color = SectionTitle)
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    placeholder = { Text(doseNotesMessage, color = Hint) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 84.dp),
                    minLines = 3,
                    maxLines = 6
                )
            }

            // 등록 버튼
            Button(
                onClick = {
                    if (userId <= 0L) {
                        onCompleted()
                        return@Button
                    }

                    // 날짜 범위 계산
                    val startMs = strToMillis(startDay) ?: System.currentTimeMillis()
                    val endMs = strToMillis(endDay) ?: startMs

                    val dayList = mutableListOf<Long>()
                    var cur = startMs
                    val oneDay = 1000L * 60 * 60 * 24
                    while (cur <= endMs) {
                        dayList.add(cur)
                        cur += oneDay
                    }

                    // 사용할 약 목록
                    val cleanMeds =
                        meds.map { it.trim() }.filter { it.isNotEmpty() }
                            .ifEmpty { listOf("약") }  // fallback

                    // 복용 시간 리스트
                    val cleanTimes =
                        intakeTimes.map { it.trim() }.filter { it.isNotEmpty() }

                    // 날짜/시간/약 조합별 Plan 생성
                    dayList.forEach { dayMs ->
                        val dayStr = dateFmt.format(Date(dayMs))

                        cleanTimes.forEach { t ->
                            val takenAtMillis = runCatching {
                                dateTimeFmt.parse("$dayStr $t")?.time
                            }.getOrNull() ?: dayMs

                            cleanMeds.forEach { medName ->
                                viewModel.createPlan(
                                    userId = userId,
                                    prescriptionId = null,
                                    medName = medName,
                                    takenAt = takenAtMillis,
                                    mealTime = mealRelation,          // before / after / none
                                    note = memo.takeIf { it.isNotBlank() },
                                    taken = null                      // 실제 복용 시간(선택값)
                                )
                            }
                        }
                    }

                    onCompleted()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(registrationComplete, color = Color.White, fontSize = 16.sp)
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

// ---------------------- Component -----------------------------

@Composable
private fun RepeatTimeRow(label: String, value: String, onChange: (String) -> Unit) {
    val timeExampleText = stringResource(R.string.time_example)
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, color = Color(0xFF6F8BA4), fontSize = 14.sp, modifier = Modifier.width(48.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(timeExampleText, color = Hint) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DateBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val selectText = stringResource(R.string.select)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Color(0xFF6F8BA4), fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CardBg)
                .clickable { onClick() },
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value.ifBlank { selectText },
                color = if (value.isNotBlank()) Color(0xFF0A0A0A) else Hint,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
        }
    }
}

@Composable
private fun SegChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else CardBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Color.White else Color(0xFF6F8BA4), fontSize = 14.sp)
    }
}

@Preview(widthDp = 392, heightDp = 1342, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun RegiScreenPreview() {
    RegiScreen(
        userId = 1L,
        prescriptionId = 101L,
        drugNames = listOf("아세트아미노펜정", "세파클러캡슐"),
        times = 3,
        days = 7
    )
}

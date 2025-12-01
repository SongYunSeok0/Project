@file:Suppress("UnusedImport")

package com.scheduler.ui

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.res.stringResource // 제거
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import com.shared.R // 제거 (필요하다면 다시 추가)
import androidx.hilt.navigation.compose.hiltViewModel
import com.domain.model.Plan
import com.scheduler.viewmodel.RegiViewModel
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
    modifier: Modifier = Modifier,
    drugNames: List<String> = emptyList(),
    times: Int? = null,
    days: Int? = null,
    viewModel: RegiViewModel = hiltViewModel(),
    onCompleted: () -> Unit = {},
    regihistoryId: Long? = null,
) {

    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { msg ->
            when (msg) {
                "등록 완료" -> {
                    Toast.makeText(context, "등록이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                    onCompleted()
                }
                "등록 실패" -> {
                    Toast.makeText(context, "등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(regihistoryId) {
        viewModel.initRegi(regihistoryId)
    }

    // ⭐ [수정] 문자열 리소스 제거하고 한글 하드코딩
    val diseaseText = "질환"
    val supplementText = "영양제"
    val diseaseNameText = "질환명"
    val supplementNameText = "영양제 이름"
    val medicationNameText = "약 이름"
    val doseDailyCount = "1일 복용 횟수"
    val doseTime = "복용 시간"
    val dosePeriod = "복용 기간"
    val startDateText = "시작일"
    val endDateText = "종료일"
    val mealRelationText = "식사 관계"
    val mealRelationBefore = "식전"
    val mealRelationAfter = "식후"
    val mealRelationIrrelevant = "식사무관"
    val memoNotesText = "메모"
    val registrationComplete = "등록 완료"
    val cancelText = "취소"
    val confirmText = "확인"
    /*
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
    */

    var tab by remember { mutableStateOf(RegiTab.DISEASE) }

    var disease by remember { mutableStateOf("") }
    var supplement by remember { mutableStateOf("") }

    val meds = remember {
        mutableStateListOf<String>().apply {
            if (drugNames.isNotEmpty()) addAll(drugNames)
            else add("")
        }
    }

    var dose by remember { mutableIntStateOf(3) }
    var mealRelation by remember { mutableStateOf("after") }
    var memo by remember { mutableStateOf("") }
    var useAlarm by remember { mutableStateOf(true) }

    val intakeTimes = remember { mutableStateListOf<String>() }

    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    fun todayStr() = dateFmt.format(Calendar.getInstance().time)
    fun strToMillis(s: String): Long? = runCatching { dateFmt.parse(s)?.time }.getOrNull()

    var startDay by remember { mutableStateOf("") }
    var endDay by remember { mutableStateOf("") }

    var showStart by remember { mutableStateOf(false) }
    var showEnd by remember { mutableStateOf(false) }

    // DatePickers (유지)
    if (showStart) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = strToMillis(startDay) ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStart = false },
            confirmButton = {
                Button(onClick = {
                    state.selectedDateMillis?.let { startDay = dateFmt.format(Date(it)) }
                    showStart = false
                }) { Text(confirmText, color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showStart = false }) { Text(cancelText) }
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
                Button(onClick = {
                    state.selectedDateMillis?.let { endDay = dateFmt.format(Date(it)) }
                    showEnd = false
                }) { Text(confirmText, color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEnd = false }) { Text(cancelText) }
            }
        ) { DatePicker(state = state) }
    }

    // LaunchedEffect 초기화 로직 (유지)
    LaunchedEffect(Unit) {
        dose = 3
        intakeTimes.clear()
        intakeTimes.addAll(presetTimes(3))
        startDay = todayStr()
        endDay = ""
    }

    LaunchedEffect(tab) {
        if (tab == RegiTab.SUPPLEMENT) {
            dose = 1
            intakeTimes.clear()
            intakeTimes.add("12:00")
        } else {
            dose = 3
            intakeTimes.clear()
            intakeTimes.addAll(presetTimes(3))
        }
    }

    LaunchedEffect(dose, tab) {
        intakeTimes.clear()
        if (tab == RegiTab.SUPPLEMENT) repeat(dose) { intakeTimes.add("") }
        else intakeTimes.addAll(presetTimes(dose))
    }

    LaunchedEffect(times) {
        if (times != null) {
            dose = times.coerceIn(1, 6)
            intakeTimes.clear()
            intakeTimes.addAll(presetTimes(dose))
        }
    }

    LaunchedEffect(days) {
        if (days != null) {
            val end = Calendar.getInstance()
            end.add(Calendar.DAY_OF_YEAR, days - 1)
            startDay = todayStr()
            endDay = dateFmt.format(end.time)
        }
    }

    LaunchedEffect(drugNames) {
        if (drugNames.isNotEmpty()) tab = RegiTab.DISEASE
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

            // 탭 (유지)
            TabRow(
                selectedTabIndex = if (tab == RegiTab.DISEASE) 0 else 1,
                containerColor = Color.Transparent,
                indicator = { pos ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(pos[if (tab == RegiTab.DISEASE) 0 else 1]),
                        height = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = tab == RegiTab.DISEASE,
                    onClick = { tab = RegiTab.DISEASE },
                    text = { Text(diseaseText) }
                )
                Tab(
                    selected = tab == RegiTab.SUPPLEMENT,
                    onClick = { tab = RegiTab.SUPPLEMENT },
                    text = { Text(supplementText) }
                )
            }

            // 병명/영양제 입력 (유지)
            if (tab == RegiTab.DISEASE) {
                Column {
                    Text(diseaseNameText, color = SectionTitle)
                    OutlinedTextField(
                        value = disease,
                        onValueChange = { disease = it },
                        placeholder = { Text("병명을 입력하세요") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Column {
                    Text(supplementNameText, color = SectionTitle)
                    OutlinedTextField(
                        value = supplement,
                        onValueChange = { supplement = it },
                        placeholder = { Text("영양제 이름을 입력하세요") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 약 이름 리스트 (유지)
            if (tab == RegiTab.DISEASE) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(medicationNameText, color = SectionTitle)
                    meds.forEachIndexed { idx, value ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = { meds[idx] = it },
                            placeholder = { Text("약 이름을 입력하세요") },
                            singleLine = true,
                            trailingIcon = {
                                if (idx == meds.lastIndex)
                                    IconButton(onClick = { meds.add("") }) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                    }
                                else if (meds.size > 1)
                                    IconButton(onClick = { meds.removeAt(idx) }) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 횟수 (유지)
            Column {
                Text(doseDailyCount, color = SectionTitle)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { dose = (dose - 1).coerceAtLeast(1) }) {
                        Icon(Icons.Default.Remove, contentDescription = null)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("${dose}회")
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { dose = (dose + 1).coerceAtMost(6) }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            }

            // 복용 시간 (유지)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(doseTime, color = SectionTitle)
                intakeTimes.forEachIndexed { i, t ->
                    TimeInputRow("${i + 1}회", t) { new -> intakeTimes[i] = new }
                }
            }

            // 기간 (유지)
            Column {
                Text(dosePeriod, color = SectionTitle)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateBox(startDateText, startDay, Modifier.weight(1f)) { showStart = true }
                    DateBox(endDateText, endDay, Modifier.weight(1f)) { showEnd = true }
                }
            }

            // 식사 관계 (유지)
            if (tab == RegiTab.DISEASE) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(mealRelationText, color = SectionTitle)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SegChip(mealRelationBefore, mealRelation == "before", Modifier.weight(1f)) { mealRelation = "before" }
                        SegChip(mealRelationAfter, mealRelation == "after", Modifier.weight(1f)) { mealRelation = "after" }
                        SegChip(mealRelationIrrelevant, mealRelation == "none", Modifier.weight(1f)) { mealRelation = "none" }
                    }
                }
            }

            // 메모 (유지)
            Column {
                Text(memoNotesText, color = SectionTitle)
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    placeholder = { Text("메모를 입력하세요") },
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 알람 (유지)
            Column {
                Text("알람 설정", color = SectionTitle)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(if (useAlarm) "알람 켜짐" else "알람 꺼짐")
                    Switch(checked = useAlarm, onCheckedChange = { useAlarm = it })
                }
            }

            // ⭐ [수정] 등록 버튼 로직 변경
            Button(
                onClick = {
                    val regiType = if (tab == RegiTab.DISEASE) "disease" else "supplement"
                    val label = if (tab == RegiTab.DISEASE) disease.ifBlank { null } else supplement.ifBlank { null }

                    // 날짜 계산 (Duration 구하기)
                    val sDate = try { LocalDate.parse(startDay) } catch (e: Exception) { LocalDate.now() }
                    val eDate = try { LocalDate.parse(endDay) } catch (e: Exception) { sDate }
                    val durationDays = ChronoUnit.DAYS.between(sDate, eDate).toInt() + 1

                    // 시간 리스트 정제
                    val realTimes = intakeTimes.mapNotNull { it.ifBlank { null } }

                    // 약 이름 정제 (일단 첫 번째 약 이름을 대표로 사용)
                    val realMeds = if (tab == RegiTab.SUPPLEMENT) {
                        listOfNotNull(supplement.ifBlank { null })
                    } else {
                        // 빈 칸 제외하고 입력된 모든 약 이름 리스트
                        meds.filter { it.isNotBlank() }
                    }

                    // ✅ [변경] 복잡한 계산 없이 파라미터만 넘김 -> 서버가 알아서 함
                    viewModel.createRegiAndSmartPlans(
                        regiType = regiType,
                        label = label,
                        issuedDate = startDay,
                        useAlarm = useAlarm,
                        startDate = startDay,
                        duration = durationDays.coerceAtLeast(1),
                        times = realTimes,
                        medNames = realMeds // ✅ [수정] listOf(medName) 대신 realMeds 전체를 전달

                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(registrationComplete, color = Color.White, fontSize = 16.sp)
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ... (나머지 TimeInputRow, DateBox 등 컴포넌트는 그대로 유지)
@Composable
fun TimeInputRow(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, modifier = Modifier.width(48.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .clickable { showPicker = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (value.isBlank()) "예: 08:00" else value,
                color = if (value.isBlank()) Hint else Color.Black,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun DateBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val selectText = "선택" // 한글로 직접 입력
    Column(modifier = modifier) {
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
                color = if (value.isNotBlank()) Color.Black else Hint,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

@Composable
private fun SegChip(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else CardBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Color.White else Color(0xFF6F8BA4)
        )
    }
}
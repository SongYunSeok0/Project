package com.scheduler.ui

import androidx.compose.foundation.BorderStroke
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
import java.text.SimpleDateFormat
import java.util.*

private val Mint = Color(0xFF6AE0D9)
private val CardBg = Color(0xFFF9FAFB)
private val SectionTitle = Color(0xFF3B566E)
private val Hint = Color(0x800A0A0A)

enum class MealRelation { BEFORE, AFTER, NONE }
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
    onSubmit: () -> Unit = {},
) {
    var tab by remember { mutableStateOf(RegiTab.DISEASE) }
    var disease by remember { mutableStateOf("") }
    var supplement by remember { mutableStateOf("") }

    val meds = remember { mutableStateListOf<String>().apply { if (drugNames.isNotEmpty()) addAll(drugNames) else add("") } }

    var dosePerDay by remember { mutableIntStateOf(3) }
    var meal by remember { mutableStateOf(MealRelation.AFTER) }
    var memo by remember { mutableStateOf("") }
    val intakeTimes = remember { mutableStateListOf<String>() }

    val fmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    fun todayStr() = fmt.format(Calendar.getInstance().time)
    fun strToMillis(s: String): Long? = runCatching { fmt.parse(s)?.time }.getOrNull()

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    // 총일수 라벨 계산(양쪽 날짜가 있으면 포함일수로 계산)
    val totalDaysLabel by remember(startDate, endDate) {
        mutableStateOf(
            run {
                val s = strToMillis(startDate)
                val e = strToMillis(endDate)
                if (s != null && e != null && e >= s) {
                    val daysInclusive = ((e - s) / (1000L * 60 * 60 * 24)).toInt() + 1
                    "(${daysInclusive}일)"
                } else "(일)"
            }
        )
    }

    // DatePickers
    var showStart by remember { mutableStateOf(false) }
    var showEnd by remember { mutableStateOf(false) }

    if (showStart) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = strToMillis(startDate) ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showStart = false },
            confirmButton = {
                // ✅ Mint 채워진 버튼
                Button(
                    onClick = {
                        state.selectedDateMillis?.let { startDate = fmt.format(Date(it)) }
                        showStart = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Mint,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("확인") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showStart = false },
                    border = BorderStroke(1.dp, Mint),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Mint
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("취소") }
            },
            // ✅ 하단 영역 흰색
            colors = DatePickerDefaults.colors(
                containerColor = Color.White
            )
        ) {
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    primary = Mint,
                    onPrimary = Color.White,
                    secondary = Mint,
                    surface = Color.White,
                    onSurface = Color(0xFF0A0A0A),
                    surfaceVariant = Color.White,
                )
            ) {
                DatePicker(
                    state = state,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.White,
                        titleContentColor = Color(0xFF0A0A0A),
                        headlineContentColor = Color(0xFF0A0A0A),
                        weekdayContentColor = Color(0xFF6F8BA4),
                        subheadContentColor = Color(0xFF6F8BA4),
                        dayContentColor = Color(0xFF0A0A0A),
                        disabledDayContentColor = Color(0xFFBDBDBD),
                        todayDateBorderColor = Mint,
                        selectedDayContainerColor = Mint,
                        selectedDayContentColor = Color.White,
                        yearContentColor = Color(0xFF0A0A0A),
                        currentYearContentColor = Mint,
                        selectedYearContainerColor = Mint,
                        selectedYearContentColor = Color.White
                    )
                )
            }
        }
    }

// END
    if (showEnd) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = strToMillis(endDate) ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEnd = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { endDate = fmt.format(Date(it)) }
                    showEnd = false
                }) { Text("확인", color = Mint) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEnd = false },
                    border = BorderStroke(1.dp, Mint),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Mint
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("취소") }
            },
            // ✅ 하단 영역 흰색
            colors = DatePickerDefaults.colors(
                containerColor = Color.White
            )
        ) {
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    primary = Mint,
                    onPrimary = Color.White,
                    secondary = Mint,
                    surface = Color.White,
                    onSurface = Color(0xFF0A0A0A),
                    surfaceVariant = Color.White,
                )
            ) {
                DatePicker(
                    state = state,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.White,
                        titleContentColor = Color(0xFF0A0A0A),
                        headlineContentColor = Color(0xFF0A0A0A),
                        weekdayContentColor = Color(0xFF6F8BA4),
                        subheadContentColor = Color(0xFF6F8BA4),
                        dayContentColor = Color(0xFF0A0A0A),
                        disabledDayContentColor = Color(0xFFBDBDBD),
                        todayDateBorderColor = Mint,
                        selectedDayContainerColor = Mint,
                        selectedDayContentColor = Color.White,
                        yearContentColor = Color(0xFF0A0A0A),
                        currentYearContentColor = Mint,
                        selectedYearContainerColor = Mint,
                        selectedYearContentColor = Color.White
                    )
                )
            }
        }
    }

    // 초기 세팅
    LaunchedEffect(Unit) {
        dosePerDay = 3
        intakeTimes.clear()
        intakeTimes.addAll(presetTimes(3))
        startDate = todayStr()
        endDate = days?.let {
            val c2 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, it.coerceAtLeast(1) - 1) }
            // 위에서 포함일수 계산이므로 종료일은 (시작+일수-1)로 세팅
            fmt.format(c2.time)
        } ?: ""
        times?.let {
            dosePerDay = it.coerceIn(1, 6)
            intakeTimes.clear()
            intakeTimes.addAll(presetTimes(dosePerDay))
        }
    }

    // 탭 변경
    LaunchedEffect(tab) {
        if (tab == RegiTab.SUPPLEMENT) {
            dosePerDay = 1
            intakeTimes.clear()
            intakeTimes.add("12:00")
            startDate = todayStr()
            endDate = ""
        } else {
            dosePerDay = 3
            intakeTimes.clear()
            intakeTimes.addAll(presetTimes(3))
            if (startDate.isBlank()) startDate = todayStr()
        }
    }

    // 횟수 변경 시 시간 자동 보정
    LaunchedEffect(dosePerDay, tab) {
        intakeTimes.clear()
        if (tab == RegiTab.SUPPLEMENT && dosePerDay == 1) intakeTimes.add("12:00")
        else intakeTimes.addAll(presetTimes(dosePerDay))
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
                containerColor = Color.Transparent,
                contentColor = SectionTitle,
                indicator = { positions ->
                    val idx = if (tab == RegiTab.DISEASE) 0 else 1
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(positions[idx]),
                        height = 3.dp,
                        color = Mint
                    )
                }
            ) {
                Tab(
                    selected = tab == RegiTab.DISEASE,
                    onClick = { tab = RegiTab.DISEASE },
                    text = { Text("질병", color = if (tab == RegiTab.DISEASE) Mint else SectionTitle) }
                )
                Tab(
                    selected = tab == RegiTab.SUPPLEMENT,
                    onClick = { tab = RegiTab.SUPPLEMENT },
                    text = { Text("영양제", color = if (tab == RegiTab.SUPPLEMENT) Mint else SectionTitle) }
                )
            }

            // 상단 메인 입력
            when (tab) {
                RegiTab.DISEASE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("병명 *", color = SectionTitle, fontSize = 14.sp)
                        OutlinedTextField(
                            value = disease,
                            onValueChange = { disease = it },
                            placeholder = { Text("병명을 입력하세요", color = Hint, fontSize = 14.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = CardBg,
                                focusedContainerColor = CardBg,
                                focusedBorderColor = Mint,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                RegiTab.SUPPLEMENT -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("영양제명 *", color = SectionTitle, fontSize = 14.sp)
                        OutlinedTextField(
                            value = supplement,
                            onValueChange = { supplement = it },
                            placeholder = { Text("영양제명을 입력하세요", color = Hint, fontSize = 14.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = CardBg,
                                focusedContainerColor = CardBg,
                                focusedBorderColor = Mint,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 약 이름 리스트: 질병 탭에서만
            if (tab == RegiTab.DISEASE) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("약 이름", color = SectionTitle, fontSize = 14.sp)
                    meds.forEachIndexed { idx, value ->
                        val isLast = idx == meds.lastIndex
                        OutlinedTextField(
                            value = value,
                            onValueChange = { meds[idx] = it },
                            placeholder = { Text("약 이름을 입력하세요", color = Hint, fontSize = 14.sp) },
                            singleLine = true,
                            trailingIcon = {
                                if (isLast) {
                                    IconButton(onClick = { meds.add("") }) {
                                        Icon(Icons.Filled.Add, contentDescription = "add", tint = Mint)
                                    }
                                } else if (meds.size > 1) {
                                    IconButton(onClick = { meds.removeAt(idx) }) {
                                        Icon(Icons.Filled.Close, contentDescription = "remove")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = CardBg,
                                focusedContainerColor = CardBg,
                                focusedBorderColor = Mint,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 복용 횟수
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("복용 횟수(하루) *", color = SectionTitle, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = {
                        if (tab == RegiTab.SUPPLEMENT) return@IconButton
                        dosePerDay = (dosePerDay - 1).coerceAtLeast(1)
                    }) { Icon(Icons.Filled.Remove, contentDescription = "minus", tint = Mint) }
                    Spacer(Modifier.weight(1f))
                    Text("${dosePerDay}회", color = Mint, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {
                        if (tab == RegiTab.SUPPLEMENT) return@IconButton
                        dosePerDay = (dosePerDay + 1).coerceAtMost(6)
                    }) { Icon(Icons.Filled.Add, contentDescription = "plus", tint = Mint) }
                }
            }

            // 복용 시간
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("복용 시간 *", color = SectionTitle, fontSize = 14.sp)
                intakeTimes.forEachIndexed { i, t ->
                    RepeatTimeRow("${i + 1}회차", t) { new -> intakeTimes[i] = new }
                }
            }

            // 복용 기간 + 총일수 라벨
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "복용 기간* ",
                        color = SectionTitle,
                        fontSize = 14.sp
                    )
                    Text(
                        text = totalDaysLabel,
                        color = Color(0xFF6F8BA4),
                        fontSize = 13.sp
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DateBox("시작일", startDate, Modifier.weight(1f)) { showStart = true }
                    DateBox("종료일", endDate, Modifier.weight(1f)) { showEnd = true }
                }
            }

            // 식사 관계: 질병 탭에서만
            if (tab == RegiTab.DISEASE) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("식사 관계", color = SectionTitle, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        SegChip("식전", selected = meal == MealRelation.BEFORE, modifier = Modifier.weight(1f)) { meal = MealRelation.BEFORE }
                        SegChip("식후", selected = meal == MealRelation.AFTER, modifier = Modifier.weight(1f)) { meal = MealRelation.AFTER }
                        SegChip("관계없음", selected = meal == MealRelation.NONE, modifier = Modifier.weight(1f)) { meal = MealRelation.NONE }
                    }
                }
            }

            // 메모
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("메모 / 주의사항", color = SectionTitle, fontSize = 14.sp)
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    placeholder = { Text("복용 시 주의사항이나 메모를 입력하세요", color = Hint, fontSize = 14.sp) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CardBg, focusedContainerColor = CardBg,
                        focusedBorderColor = Mint, unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 84.dp),
                    minLines = 3, maxLines = 6
                )
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Mint)
            ) { Text("등록 완료", color = Color.White, fontSize = 16.sp) }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun RepeatTimeRow(label: String, value: String, onChange: (String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, color = Color(0xFF6F8BA4), fontSize = 14.sp, modifier = Modifier.width(48.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text("예: 08:00", color = Hint, fontSize = 14.sp) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = CardBg, focusedContainerColor = CardBg,
                focusedBorderColor = Mint, unfocusedBorderColor = Color.Transparent
            ),
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
                text = value.ifBlank { "선택" },
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
            .background(if (selected) Mint else CardBg)
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
        drugNames = listOf("아세트아미노펜정", "세파클러캡슐"),
        times = 3,
        days = 7
    )
}

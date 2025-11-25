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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shared.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.domain.model.Plan
import com.scheduler.viewmodel.RegiViewModel
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
    modifier: Modifier = Modifier,
    drugNames: List<String> = emptyList(),
    viewModel: RegiViewModel = hiltViewModel(),
    onCompleted: () -> Unit = {},
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
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

    var tab by remember { mutableStateOf(RegiTab.DISEASE) }

    var disease by remember { mutableStateOf("") }
    var supplement by remember { mutableStateOf("") }

    val meds = remember {
        mutableStateListOf<String>().apply {
            if (drugNames.isNotEmpty()) addAll(drugNames) else add("")
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

    // DatePicker ------------------------------------------------
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

    // 초기값 -----------------------------------------------------
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

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(doseTime, color = SectionTitle)
                intakeTimes.forEachIndexed { i, t ->
                    RepeatTimeRow("${i + 1}회", t) { new -> intakeTimes[i] = new }
                }
            }

            Column {
                Text(dosePeriod, color = SectionTitle)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateBox(startDateText, startDay, Modifier.weight(1f)) { showStart = true }
                    DateBox(endDateText, endDay, Modifier.weight(1f)) { showEnd = true }
                }
            }

            if (tab == RegiTab.DISEASE) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(mealRelationText, color = SectionTitle)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SegChip(mealRelationBefore, mealRelation == "before", Modifier.weight(1f)) {
                            mealRelation = "before"
                        }
                        SegChip(mealRelationAfter, mealRelation == "after", Modifier.weight(1f)) {
                            mealRelation = "after"
                        }
                        SegChip(mealRelationIrrelevant, mealRelation == "none", Modifier.weight(1f)) {
                            mealRelation = "none"
                        }
                    }
                }
            }

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
                    Switch(
                        checked = useAlarm,
                        onCheckedChange = { useAlarm = it }
                    )
                }
            }

            // ★★★ 등록 버튼, 핵심 수정 완료 ★★★
            Button(
                onClick = {
                    val regiType = if (tab == RegiTab.DISEASE) "disease" else "supplement"
                    val label = if (tab == RegiTab.DISEASE)
                        disease.ifBlank { null }
                    else
                        supplement.ifBlank { null }

                    val sMs = strToMillis(startDay) ?: System.currentTimeMillis()
                    val eMs = strToMillis(endDay) ?: sMs
                    val oneDay = 86400000L

                    // 생성할 날짜 리스트
                    val daysList = buildList {
                        var cur = sMs
                        while (cur <= eMs) {
                            add(cur)
                            cur += oneDay
                        }
                    }

                    val realMeds =
                        if (tab == RegiTab.SUPPLEMENT)
                            listOfNotNull(supplement.ifBlank { null })
                        else
                            meds.mapNotNull { it.ifBlank { null } }

                    val realTimes = intakeTimes.mapNotNull { it.ifBlank { null } }

                    val plans = mutableListOf<Plan>()

                    val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val dfDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    // ★ 오늘 00:00 기준 ms (중복 보정 방지)
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val todayStartMs = cal.timeInMillis     // ★ 수정됨

                    daysList.forEach { d ->
                        val ds = dfDay.format(Date(d))

                        realTimes.forEach { t ->
                            val takenAt0 = df.parse("$ds $t")?.time ?: d

                            // ★ 수정된 보정 로직
                            val takenAt =
                                if (takenAt0 < todayStartMs) takenAt0 + oneDay
                                else takenAt0

                            realMeds.forEach { med ->
                                plans += Plan(
                                    id = 0L,
                                    regihistoryId = null,
                                    medName = med,
                                    takenAt = takenAt,
                                    mealTime = mealRelation,
                                    note = memo.ifBlank { null },
                                    taken = null,
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
                        plans = plans
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

@Composable
private fun RepeatTimeRow(label: String, value: String, onChange: (String) -> Unit) {
    val timeExampleText = stringResource(R.string.time_example)
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, modifier = Modifier.width(48.dp))
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
        Text(text, color = if (selected) Color.White else Color(0xFF6F8BA4))
    }
}

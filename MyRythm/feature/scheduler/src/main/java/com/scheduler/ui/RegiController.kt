package com.scheduler.ui

import androidx.compose.runtime.*
import com.domain.model.Plan
import com.scheduler.viewmodel.RegiViewModel
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

// 0115 기존 RegiScreen.kt의 private 제거, 컨트롤러로 이동
enum class RegiTab { DISEASE, SUPPLEMENT }

class RegiController(
    val drugNames: List<String>,
    val times: Int?,
    val days: Int?,
    val regihistoryId: Long?,
    val viewModel: RegiViewModel
) {
    var tab by mutableStateOf(RegiTab.DISEASE)

    var disease by mutableStateOf("")
    var supplement by mutableStateOf("")

    val meds = mutableStateListOf<String>().apply {
        if (drugNames.isNotEmpty()) addAll(drugNames) else add("")
    }

    var initialized by mutableStateOf(false)

    var dose by mutableIntStateOf(3)
    val intakeTimes = mutableStateListOf<String>()

    var mealRelation by mutableStateOf("after")
    var memo by mutableStateOf("")
    var useAlarm by mutableStateOf(true)

    // 0115 기존의 val, fun -> private 변경
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private fun todayStr() = dateFmt.format(Calendar.getInstance().time)
    private fun strToMillis(s: String): Long? = runCatching { dateFmt.parse(s)?.time }.getOrNull()

    var startDay by mutableStateOf("")
    var endDay by mutableStateOf("")

    // 0115 RegiScreen.kt의 IoT 기기 선택 상태 컨트롤러로 이동
    var selectedDevice by mutableStateOf<Long?>(null)

    var showStart by mutableStateOf(false)
    var showEnd by mutableStateOf(false)


    // 최초 1회 초기화 로직은 컨트롤러로 이동, LaunchedEffect는 스크린에.
    fun init() {
        if (initialized) return
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

    private fun presetTimes(n: Int): List<String> = when (n) {
        1 -> listOf("08:00")
        2 -> listOf("08:00", "18:00")
        3 -> listOf("08:00", "12:00", "18:00")
        4 -> listOf("08:00", "12:00", "18:00", "22:00")
        else -> List(n) { "" }
    }

    // 0115 탭 변경 시 로직. 기존 LaunchedEffect(tab) 로직
    fun handleTabChange() {
        if (!initialized) return
        intakeTimes.clear()
        if (tab == RegiTab.SUPPLEMENT) {
            dose = 1
            intakeTimes.add("12:00")
        } else {
            dose = 3
            intakeTimes.addAll(presetTimes(3))
        }
    }

    // 복용 횟수 변경
    fun onDoseChange(newDose: Int) {
        dose = newDose.coerceIn(1, 6)
        syncIntakeTimes()
    }

    // 0115 복용 시간 동기화 로직. 기존 LaunchedEffect(dose) 로직
    fun syncIntakeTimes() {
        if (intakeTimes.size < dose) {
            repeat(dose - intakeTimes.size) { intakeTimes.add("") }
        } else if (intakeTimes.size > dose) {
            repeat(intakeTimes.size - dose) { intakeTimes.removeAt(intakeTimes.lastIndex) }
        }
    }

    // 등록 버튼의 onClick 로직 그대로 유지
    fun submit() {
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
                        regihistoryLabel = label,
                        medName = med,
                        takenAt = takenAt,
                        mealTime = mealRelation,
                        note = memo.ifBlank { null },
                        taken = null,
                        takenTime = null,
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
    }
}

@Composable
fun rememberRegiController(
    drugNames: List<String>,
    times: Int?,
    days: Int?,
    regihistoryId: Long?,
    viewModel: RegiViewModel
) = remember {
    RegiController(drugNames, times, days, regihistoryId, viewModel)
}
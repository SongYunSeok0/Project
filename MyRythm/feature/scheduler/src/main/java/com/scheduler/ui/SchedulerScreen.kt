package com.scheduler.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.scheduler.viewmodel.PlanViewModel
import com.shared.R
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import kotlinx.coroutines.launch

// 컬러
private val Mint = Color(0xFF6AE0D9)
private val Yellow = Color(0xFFF9C034)
private val GrayText = Color(0xFF6A7282)
private val BG = Color(0xFFFCF8FF)

// 데이터 모델
enum class IntakeStatus { DONE, SCHEDULED }
data class MedItem(
    val planIds: List<Long>,          // 여러 Plan의 ID 리스트
    val label: String,
    val medNames: List<String>,       // 여러 약 이름 리스트
    val time: String,
    val mealTime: String?,
    val memo: String?,
    val useAlarm: Boolean,
    val status: IntakeStatus
)


//  외부에서 호출되는 Screen
@Composable
fun SchedulerScreen(
    userId: Long,
    vm: PlanViewModel = hiltViewModel(),
    onOpenRegi: () -> Unit = {}
) {
    val ui by vm.uiState.collectAsState()
    val items by vm.itemsByDate.collectAsState(initial = emptyMap())

    LaunchedEffect(userId) {
        if (userId > 0L) vm.load(userId)
        else Log.e("SchedulerScreen", " userId 누락: '$userId'")
    }

    SchedulerContent(
        itemsByDate = items,
        resetKey = ui.plans.hashCode(),
        onToggleAlarm = { planIds, newValue ->
            planIds.forEach { planId ->
                vm.toggleAlarm(userId, planId, newValue)
            }
        }
    )
}


//  내부 Content
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SchedulerContent(
    itemsByDate: Map<LocalDate, List<MedItem>> = emptyMap(),
    clock: Clock = Clock.systemDefaultZone(),
    resetKey: Any? = null,
    onToggleAlarm: (List<Long>, Boolean) -> Unit = { _, _ -> }
) {
    val monthSuffix = stringResource(R.string.month_suffix)
    val weekSuffix = stringResource(R.string.week_suffix)
    val dateFormat = stringResource(R.string.format_date_month_day)
    val upcomingText = stringResource(R.string.status_upcoming)
    val alarmOffText = stringResource(R.string.alarm_off)
    val emptyMessage = stringResource(R.string.scheduler_message_schedule_empty)

    val today = remember(clock) { LocalDate.now(clock) }
    var weekAnchor by remember { mutableStateOf(today) }
    var selectedDay by remember { mutableStateOf(today) }
    var selectedItem by remember { mutableStateOf<MedItem?>(null) }

    val startPage = 5000
    val pagerState = rememberPagerState(initialPage = startPage, pageCount = { 10000 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(resetKey, today) {
        weekAnchor = today
        selectedDay = today
        pagerState.scrollToPage(startPage)
    }

    LaunchedEffect(pagerState.currentPage, today) {
        val diff = pagerState.currentPage - startPage
        weekAnchor = today.plusWeeks(diff.toLong())

        val week = weekRangeOf(weekAnchor)
        if (selectedDay !in week) selectedDay = week.first()
    }

    val dayItems by remember(selectedDay, itemsByDate) {
        mutableStateOf(itemsByDate[selectedDay].orEmpty())
    }

    Scaffold(
        containerColor = BG,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->

        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BG)
                .verticalScroll(rememberScrollState())
        ) {

            val wf = WeekFields.of(Locale.KOREAN)
            val startOfWeek = weekRangeOf(weekAnchor).first()
            val weekNum = weekAnchor.get(wf.weekOfMonth())
            val title = "${startOfWeek.monthValue}$monthSuffix ${weekNum}$weekSuffix"

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    stringResource(id = R.string.sunday),
                    stringResource(id = R.string.monday),
                    stringResource(id = R.string.tuesday),
                    stringResource(id = R.string.wednesday),
                    stringResource(id = R.string.thursday),
                    stringResource(id = R.string.friday),
                    stringResource(id = R.string.saturday)
                ).forEach {
                    Text(it, color = Color(0xFF999999), fontSize = 12.sp)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) { page ->

                val anchor = today.plusWeeks((page - startPage).toLong())
                val week = weekRangeOf(anchor)

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    week.forEach { day ->

                        val isSelected = (day == selectedDay)
                        val isToday = (day == today)

                        val bg = when {
                            isToday && isSelected -> Mint
                            isToday -> Mint.copy(alpha = 0.4f)
                            isSelected -> Yellow
                            else -> Color.Transparent
                        }

                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .clickable {
                                    selectedDay = day
                                    val diffWeeks =
                                        java.time.temporal.ChronoUnit.WEEKS.between(today, day)
                                            .toInt()
                                    val target = startPage + diffWeeks
                                    if (pagerState.currentPage != target) {
                                        scope.launch { pagerState.animateScrollToPage(target) }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${day.dayOfMonth}",
                                fontSize = 16.sp,
                                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isSelected || isToday) Color.Black else Color(0xFF2B2B2B)
                            )
                        }
                    }
                }
            }

            Text(
                selectedDay.format(
                    DateTimeFormatter.ofPattern(dateFormat, Locale.KOREAN)
                ),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF101828),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            Column(
                Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                if (dayItems.isEmpty()) {
                    Text(emptyMessage, color = GrayText, fontSize = 13.sp)
                } else {
                    dayItems.sortedBy { it.time }.forEach { item ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { selectedItem = item },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (item.status == IntakeStatus.DONE) Mint
                                                else Color(0xFFD1D5DC)
                                            )
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(item.label, fontSize = 15.sp, color = Color(0xFF101828))
                                        Text(item.time, fontSize = 13.sp, color = GrayText)
                                    }
                                }

                                Text(
                                    if (item.useAlarm) upcomingText else alarmOffText,
                                    color = Color(0xFF999999),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 상세보기 다이얼로그
    selectedItem?.let { item ->
        MedDetailDialog(
            item = item,
            onDismiss = { selectedItem = null },
            onToggleAlarm = { newValue ->
                onToggleAlarm(item.planIds, newValue)
                selectedItem = item.copy(useAlarm = newValue)
            }
        )
    }
}

@Composable
fun MedDetailDialog(
    item: MedItem,
    onDismiss: () -> Unit,
    onToggleAlarm: (Boolean) -> Unit
) {
    val detailTitle = stringResource(R.string.detail_title)
    val regiLabel = stringResource(R.string.regi_label)
    val medNameLabel = stringResource(R.string.med_name_label)
    val mealTimeLabel = stringResource(R.string.meal_time_label)
    val memoLabel = stringResource(R.string.memo_label)
    val alarmLabel = stringResource(R.string.alarm_label)
    val closeText = stringResource(R.string.close)

    val mealTimeText = when(item.mealTime) {
        "before" -> stringResource(R.string.meal_relation_before)
        "after" -> stringResource(R.string.meal_relation_after)
        "none" -> stringResource(R.string.meal_relation_irrelevant)
        else -> "-"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 600.dp) // 최대 높이 제한
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()) // 스크롤 가능하게
            ) {
                Text(
                    detailTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                DetailRow(regiLabel, item.label)

                // 약 이름이 여러 개일 경우 세로로 표시
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        medNameLabel,
                        fontSize = 14.sp,
                        color = GrayText,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    item.medNames.forEach { medName ->
                        Text(
                            "• $medName",
                            fontSize = 14.sp,
                            color = Color(0xFF101828),
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }
                }

                DetailRow(mealTimeLabel, mealTimeText)
                DetailRow(memoLabel, item.memo ?: "-")

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        alarmLabel,
                        fontSize = 14.sp,
                        color = GrayText
                    )
                    Switch(
                        checked = item.useAlarm,
                        onCheckedChange = onToggleAlarm
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(closeText)
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
            fontSize = 14.sp,
            color = GrayText,
            modifier = Modifier.width(80.dp)
        )
        Text(
            value,
            fontSize = 14.sp,
            color = Color(0xFF101828)
        )
    }
}

// ─────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────
private fun weekRangeOf(anchor: LocalDate): List<LocalDate> {
    val start = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    return (0..6).map { start.plusDays(it.toLong()) }
}
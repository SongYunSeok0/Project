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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val label: String,
    val time: String,
    val status: IntakeStatus
)


// ─────────────────────────────────────────────
//  외부에서 호출되는 Screen
// ─────────────────────────────────────────────
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
        resetKey = ui.plans.hashCode()
    )
}


// ─────────────────────────────────────────────
//  내부 Content
// ─────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SchedulerContent(
    itemsByDate: Map<LocalDate, List<MedItem>> = emptyMap(),
    clock: Clock = Clock.systemDefaultZone(),
    resetKey: Any? = null
) {
    val monthSuffix = stringResource(R.string.month_suffix)
    val weekSuffix = stringResource(R.string.week_suffix)
    val dateFormat = stringResource(R.string.format_date_month_day)
    val upcomingText = stringResource(R.string.status_upcoming)
    val emptyMessage = stringResource(R.string.scheduler_message_schedule_empty)

    val today = remember(clock) { LocalDate.now(clock) }
    var weekAnchor by remember { mutableStateOf(today) }
    var selectedDay by remember { mutableStateOf(today) }

    val startPage = 5000
    val pagerState = rememberPagerState(initialPage = startPage, pageCount = { 10000 })
    val scope = rememberCoroutineScope()

    // 주차 리셋
    LaunchedEffect(resetKey, today) {
        weekAnchor = today
        selectedDay = today
        pagerState.scrollToPage(startPage)
    }

    // 페이지 이동 → 날짜 갱신
    LaunchedEffect(pagerState.currentPage, today) {
        val diff = pagerState.currentPage - startPage
        weekAnchor = today.plusWeeks(diff.toLong())

        val week = weekRangeOf(weekAnchor)
        if (selectedDay !in week) selectedDay = week.first()
    }

    val dayItems by remember(selectedDay, itemsByDate) {
        mutableStateOf(itemsByDate[selectedDay].orEmpty())
    }

    // Scaffold
    Scaffold(
        containerColor = BG,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->

        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BG)
        ) {

            // ───────────────────────
            //  주차 타이틀
            // ───────────────────────
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

            // ───────────────────────
            //  요일 Row
            // ───────────────────────
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

            // ───────────────────────
            //  주간 Pager
            // ───────────────────────
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
                                        java.time.temporal.ChronoUnit.WEEKS.between(today, day).toInt()
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

            // ───────────────────────
            //  날짜 헤더
            // ───────────────────────
            Text(
                selectedDay.format(DateTimeFormatter.ofPattern(dateFormat, Locale.KOREAN)),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF101828),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            // ───────────────────────
            //  복용 일정 리스트 (Card 하나씩)
            // ───────────────────────
            Column(
                Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                if (dayItems.isEmpty()) {
                    Text(emptyMessage, color = GrayText, fontSize = 13.sp)
                } else {
                    dayItems.sortedBy { it.time }.forEach { item ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
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
                                    upcomingText,
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
}


// ─────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────
private fun weekRangeOf(anchor: LocalDate): List<LocalDate> {
    val start = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    return (0..6).map { start.plusDays(it.toLong()) }
}


@Preview(showBackground = true)
@Composable
fun Preview_SchedulerContent() {

    val today = LocalDate.now()
    val demoItems = mapOf(
        today to listOf(
            MedItem(label = "오메가3", time = "08:00", status = IntakeStatus.SCHEDULED),
            MedItem(label = "비타민D", time = "12:00", status = IntakeStatus.DONE)
        )
    )

    SchedulerContent(
        itemsByDate = demoItems,
        resetKey = 0
    )
}

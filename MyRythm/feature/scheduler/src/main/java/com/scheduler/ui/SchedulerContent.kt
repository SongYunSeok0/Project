package com.scheduler.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scheduler.ui.components.MedDetailDialog
import com.shared.R
import com.shared.ui.theme.AppTheme
import com.shared.ui.theme.componentTheme
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * 내부 Content (캘린더 UI + 상태 관리)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SchedulerContent(
    itemsByDate: Map<LocalDate, List<MedItem>> = emptyMap(),
    clock: Clock = Clock.systemDefaultZone(),
    resetKey: Any? = null,
    isDeviceUser: Boolean = false,
    onToggleAlarm: (List<Long>, Boolean) -> Unit = { _, _ -> },
    onMarkTaken: (List<Long>) -> Unit = {}          // 🔥 복용 완료 콜백 추가
) {
    val monthSuffix = stringResource(R.string.month_suffix)
    val weekSuffix = stringResource(R.string.week_suffix)
    val dateFormat = stringResource(R.string.format_date_month_day)
    val upcomingText = stringResource(R.string.status_upcoming)
    val alarmOffText = stringResource(R.string.alarm_off)
    val emptyMessage = stringResource(R.string.scheduler_message_schedule_empty)

    // 상태 텍스트
    val doneText = stringResource(R.string.status_done)
    val missedText = stringResource(R.string.status_missed)

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
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->

        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {

            val wf = WeekFields.of(Locale.KOREAN)
            val weekNum = weekAnchor.get(wf.weekOfMonth())
            val title = "${weekAnchor.monthValue}$monthSuffix ${weekNum}$weekSuffix"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.width(48.dp))

                Text(
                    title,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = {
                        selectedDay = today
                        weekAnchor = today
                        scope.launch {
                            pagerState.animateScrollToPage(startPage)
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.down_chevron),
                        contentDescription = "오늘로 돌아가기",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
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
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 12.sp
                    )
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
                            isToday && isSelected -> MaterialTheme.colorScheme.primary
                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            isSelected -> MaterialTheme.componentTheme.bookMarkColor
                            else -> MaterialTheme.colorScheme.secondary
                        }

                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(bg)
                                .clickable {
                                    selectedDay = day

                                    val todayWeekStart =
                                        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                                    val dayWeekStart =
                                        day.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

                                    val diffWeeks = java.time.temporal.ChronoUnit.WEEKS
                                        .between(todayWeekStart, dayWeekStart)
                                        .toInt()

                                    val target = startPage + diffWeeks

                                    if (pagerState.currentPage != target) {
                                        scope.launch {
                                            pagerState.animateScrollToPage(target)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${day.dayOfMonth}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isSelected || isToday)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            Text(
                selectedDay.format(
                    DateTimeFormatter.ofPattern(dateFormat, Locale.KOREAN)
                ),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            Column(
                Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                if (dayItems.isEmpty()) {
                    Text(
                        emptyMessage,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                } else {
                    dayItems.sortedBy { it.time }.forEach { item ->

                        val dotColor = when (item.status) {
                            IntakeStatus.DONE ->
                                MaterialTheme.colorScheme.primary
                            IntakeStatus.MISSED ->
                                MaterialTheme.colorScheme.error
                            IntakeStatus.SCHEDULED ->
                                MaterialTheme.colorScheme.surfaceVariant
                        }

                        val statusText = when (item.status) {
                            IntakeStatus.DONE   -> doneText
                            IntakeStatus.MISSED -> missedText
                            IntakeStatus.SCHEDULED ->
                                if (item.useAlarm) upcomingText else alarmOffText
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { selectedItem = item },
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.background
                            ),
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
                                            .background(dotColor)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            item.label,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            item.time,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = statusText,
                                    color = MaterialTheme.colorScheme.outline,
                                    style = MaterialTheme.typography.labelSmall,
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
            isDeviceUser = isDeviceUser,
            onDismiss = { selectedItem = null },
            onToggleAlarm = { newValue ->
                onToggleAlarm(item.planIds, newValue)
                selectedItem = item.copy(useAlarm = newValue)
            },
            onMarkTaken = {
                onMarkTaken(item.planIds)
                selectedItem = null
            }
        )
    }
}

// Helpers
private fun weekRangeOf(anchor: LocalDate): List<LocalDate> {
    val start = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    return (0..6).map { start.plusDays(it.toLong()) }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun Preview_SchedulerContent_Full() {
    AppTheme {
        val today = LocalDate.now()

        val demoItems = mapOf(
            today to listOf(
                MedItem(
                    planIds = listOf(1L),
                    label = "오메가3",
                    medNames = listOf("오메가3 1000mg"),
                    time = "08:00",
                    mealTime = "after",
                    memo = "식후 복용",
                    useAlarm = true,
                    status = IntakeStatus.SCHEDULED
                ),
                MedItem(
                    planIds = listOf(2L),
                    label = "비타민D",
                    medNames = listOf("비타민D 5000IU"),
                    time = "12:00",
                    mealTime = "before",
                    memo = null,
                    useAlarm = false,
                    status = IntakeStatus.DONE
                ),
                MedItem(
                    planIds = listOf(3L),
                    label = "혈압약",
                    medNames = listOf("암로디핀 5mg"),
                    time = "21:00",
                    mealTime = "after",
                    memo = null,
                    useAlarm = true,
                    status = IntakeStatus.MISSED
                )
            )
        )

        SchedulerContent(
            itemsByDate = demoItems,
            resetKey = 0,
            onToggleAlarm = { _, _ -> },
            onMarkTaken = { _ -> }
        )
    }
}
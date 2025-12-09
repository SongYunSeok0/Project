package com.scheduler.ui

import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.scheduler.viewmodel.PlanViewModel
import com.shared.R
import com.shared.navigation.MainRoute
import com.shared.ui.components.AppButton
import com.shared.ui.theme.AppFieldHeight
import com.shared.ui.theme.AppTheme
import com.shared.ui.theme.componentTheme
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import kotlinx.coroutines.launch

// 데이터 모델
enum class IntakeStatus { DONE, SCHEDULED, MISSED }

data class MedItem(
    val planIds: List<Long>,
    val label: String,
    val medNames: List<String>,
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
    navController: NavController,
    vm: PlanViewModel = hiltViewModel(),
    onOpenRegi: () -> Unit = {}
) {
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val items by vm.itemsByDate.collectAsStateWithLifecycle()
    val isDeviceUser by vm.isDeviceUser.collectAsStateWithLifecycle()

    BackHandler {
        navController.navigate(MainRoute) {
            popUpTo(MainRoute) { inclusive = false }
            launchSingleTop = true
        }
    }

    LaunchedEffect(userId) {
        if (userId > 0L) vm.load(userId)
        else Log.e("SchedulerScreen", " userId 누락: '$userId'")
    }

    SchedulerContent(
        itemsByDate = items,
        resetKey = ui.plans.hashCode(),
        isDeviceUser = isDeviceUser,
        onToggleAlarm = { planIds, newValue ->
            planIds.forEach { planId ->
                vm.toggleAlarm(userId, planId, newValue)
            }
        },
        onMarkTaken = { planIds ->
            // 한 카드에 묶인 모든 Plan을 복용 완료 처리
            planIds.forEach { planId ->
                vm.markAsTaken(userId, planId)
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

@Composable
fun MedDetailDialog(
    item: MedItem,
    onDismiss: () -> Unit,
    onToggleAlarm: (Boolean) -> Unit,
    isDeviceUser: Boolean = false,
    onMarkTaken: (() -> Unit)? = null       // 복용 완료 처리 콜백
) {
    val detailTitle = stringResource(R.string.detail_title)
    val regiLabel = stringResource(R.string.regi_label)
    val medNameLabel = stringResource(R.string.med_name_label)
    val mealTimeLabel = stringResource(R.string.meal_relation)
    val noMemoLabel = stringResource(R.string.no_memo)
    val memoLabel = stringResource(R.string.memo_label)
    val alarmLabel = stringResource(R.string.alarm_label)
    val closeText = stringResource(R.string.close)

    val mealTimeText = when (item.mealTime) {
        "before" -> stringResource(R.string.meal_relation_before)
        "after" -> stringResource(R.string.meal_relation_after)
        "none" -> stringResource(R.string.meal_relation_irrelevant)
        else -> "-"
    }

    // 🔒 알림 스위치: SCHEDULED 상태에서만 켤/끌 수 있음
    //  - 기기 연동 사용자(isDeviceUser)는 항상 비활성화
    val alarmSwitchEnabled =
        !isDeviceUser && item.status == IntakeStatus.SCHEDULED

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    detailTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                DetailRow(regiLabel, item.label)
                DetailMedNames(medNameLabel, item.medNames)
                DetailRow(mealTimeLabel, mealTimeText)
                DetailRow(memoLabel, item.memo ?: noMemoLabel)

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        alarmLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Switch(
                        checked = item.useAlarm,
                        enabled = alarmSwitchEnabled,
                        onCheckedChange = {
                            if (alarmSwitchEnabled) {
                                onToggleAlarm(it)
                            }
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))

                if (item.status == IntakeStatus.MISSED ||
                    item.status == IntakeStatus.SCHEDULED) {
                    // 🔹 미복용 / 예정 일정: 복용 완료 + 닫기 버튼 나란히
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppButton(
                            text = "복용 완료",
                            modifier = Modifier
                                .weight(1f)
                                .height(AppFieldHeight),
                            shape = MaterialTheme.shapes.medium,
                            onClick = {
                                onMarkTaken?.invoke()
                            }
                        )
                        AppButton(
                            text = closeText,
                            modifier = Modifier
                                .weight(1f)
                                .height(AppFieldHeight),
                            shape = MaterialTheme.shapes.medium,
                            onClick = onDismiss
                        )
                    }
                } else {
                    // 🔹 DONE: 닫기만 전체 폭
                    AppButton(
                        text = closeText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppFieldHeight),
                        shape = MaterialTheme.shapes.medium,
                        onClick = onDismiss
                    )
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
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DetailMedNames(label: String, medNames: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.width(80.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (medNames.isEmpty() || medNames.all { it.isBlank() }) {
                Text(
                    "-",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                medNames.forEach { name ->
                    Text(
                        text = "• $name",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
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

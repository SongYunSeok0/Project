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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
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

// 컬러
private val Yellow = Color(0xFFF9C034)


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
    navController: NavController,
    vm: PlanViewModel = hiltViewModel(),
    onOpenRegi: () -> Unit = {}
) {
    val ui by vm.uiState.collectAsState()
    val items by vm.itemsByDate.collectAsState(initial = emptyMap())

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
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->

        // ★ 화면 전체를 스크롤 가능하도록 변경
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
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
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }

                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(MaterialTheme.shapes.medium)
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
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isSelected || isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { selectedItem = item },
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
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
                                                if (item.status == IntakeStatus.DONE) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
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
                                    if (item.useAlarm) upcomingText else alarmOffText,
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
            colors = CardDefaults.cardColors(containerColor =MaterialTheme.colorScheme.background),
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                DetailRow(regiLabel,item.label)

                // 약 이름이 여러 개일 경우 세로로 표시
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        medNameLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    item.medNames.forEach { medName ->
                        Text(
                            "• $medName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
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
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Switch(
                        checked = item.useAlarm,
                        onCheckedChange = onToggleAlarm
                    )
                }

                Spacer(Modifier.height(20.dp))

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

//  Helpers
private fun weekRangeOf(anchor: LocalDate): List<LocalDate> {
    val start = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    return (0..6).map { start.plusDays(it.toLong()) }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun Preview_SchedulerContent_Full() {

    AppTheme {

        // ---- 데모 날짜/아이템 구성 ----
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
                )
            ),
            today.plusDays(1) to listOf(
                MedItem(
                    planIds = listOf(3L),
                    label = "유산균",
                    medNames = listOf("락토핏", "듀오락"),
                    time = "07:30",
                    mealTime = "none",
                    memo = "아침 물과 함께",
                    useAlarm = true,
                    status = IntakeStatus.SCHEDULED
                )
            )
        )

        // ---- 콘텐츠 호출 ----
        SchedulerContent(
            itemsByDate = demoItems,
            resetKey = 0,
            onToggleAlarm = { _, _ -> }
        )
    }
}

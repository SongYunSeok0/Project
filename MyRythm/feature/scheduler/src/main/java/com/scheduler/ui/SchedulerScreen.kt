@file:Suppress("UnusedImport")

package com.scheduler.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scheduler.viewmodel.PlanViewModel
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

private val Mint = Color(0xFF6AE0D9)
private val MintDark = Color(0xFF5DB0A8)
private val Yellow = Color(0xFFF9C034)
private val GrayText = Color(0xFF6A7282)
private val BG = Color(0xFFFCF8FF)

enum class IntakeStatus { DONE, SCHEDULED }
data class MedItem(val name: String, val time: String, val status: IntakeStatus)

@Composable
fun SchedulerScreen(
    userId: String?,                // NavGraph에서 문자열로 전달
    vm: PlanViewModel = hiltViewModel(),
    onOpenRegi: () -> Unit = {}     // ✅ NavGraph에서 주입받아 아래로 내려줌
) {
    val ui by vm.uiState.collectAsState()
    val items by vm.itemsByDate.collectAsState(initial = emptyMap())

    LaunchedEffect(userId) {
        if (!userId.isNullOrBlank()) {
            vm.load(userId)         // ✅ PlanViewModel.load(String) 버전 사용
        } else {
            Log.e("SchedulerScreen", "❌ userId 누락: '$userId'")
        }
    }

    SchedulerContent(
        itemsByDate = items,
        resetKey = ui.plans.hashCode(),
        onOpenRegi = onOpenRegi     // ✅ 아래로 전달
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SchedulerContent(
    itemsByDate: Map<LocalDate, List<MedItem>> = emptyMap(),
    clock: Clock = Clock.systemDefaultZone(),
    resetKey: Any? = null,
    onOpenRegi: () -> Unit = {}
) {
    val today = remember(clock) { LocalDate.now(clock) }
    var weekAnchor by remember { mutableStateOf(today) }
    var selected by remember { mutableStateOf(today) }

    val startPage = 5000
    val pagerState = rememberPagerState(initialPage = startPage, pageCount = { 10000 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(resetKey, today) {
        weekAnchor = today
        selected = today
        pagerState.scrollToPage(startPage)
    }

    LaunchedEffect(pagerState.currentPage, today) {
        val delta = pagerState.currentPage - startPage
        weekAnchor = today.plusWeeks(delta.toLong())
        val currentWeek = weekRangeOf(weekAnchor)
        if (selected !in currentWeek) selected = currentWeek.first()
    }

    val dayItems by remember(selected, itemsByDate) {
        mutableStateOf(itemsByDate[selected].orEmpty())
    }
    val banner = remember(dayItems) { bannerInfo(dayItems) }

    Scaffold(containerColor = BG, contentWindowInsets = WindowInsets(0, 0, 0, 0)) { inner ->
        Column(
            Modifier.fillMaxSize().padding(inner).background(BG)
        ) {
            val wf = WeekFields.of(Locale.KOREAN)
            val startOfWeek = weekRangeOf(weekAnchor).first()
            val weekNum = weekAnchor.get(wf.weekOfMonth())
            val title = "${startOfWeek.monthValue}월 ${weekNum}주차"

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach {
                    Text(it, color = Color(0xFF999999), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val anchor = today.plusWeeks((page - startPage).toLong())
                val week = weekRangeOf(anchor)

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    week.forEach { day ->
                        val isSelected = day == selected
                        val isToday = day == today

                        val bgColor = when {
                            isToday && isSelected -> Mint
                            isToday -> Mint.copy(alpha = 0.4f)
                            isSelected -> Yellow
                            else -> Color.Transparent
                        }

                        Box(
                            Modifier.size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .clickable {
                                    selected = day
                                    val weekDelta =
                                        java.time.temporal.ChronoUnit.WEEKS.between(today, day).toInt()
                                    val target = startPage + weekDelta
                                    if (pagerState.currentPage != target) {
                                        scope.launch { pagerState.animateScrollToPage(target) }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${day.dayOfMonth}",
                                fontSize = 16.sp,
                                color = if (isSelected || isToday) Color.Black else Color(0xFF2B2B2B),
                                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Card(
                Modifier.padding(24.dp).fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        selected.format(DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)),
                        fontSize = 16.sp,
                        lineHeight = 1.5.em,
                        color = Color(0xFF101828)
                    )
                    Spacer(Modifier.height(16.dp))
                    if (dayItems.isEmpty()) {
                        Text("등록된 복약 일정이 없습니다", color = GrayText, fontSize = 13.sp)
                    } else {
                        dayItems.sortedBy { it.time }.forEachIndexed { i, it ->
                            PillRow(
                                dot = if (it.status == IntakeStatus.DONE) Mint else Color(0xFFD1D5DC),
                                title = it.name,
                                time = it.time,
                                trailing = {
                                    if (it.status == IntakeStatus.DONE) DoneText()
                                    else Text("예정", color = Color(0xFF999999), fontSize = 12.sp)
                                }
                            )
                            if (i != dayItems.lastIndex) Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }

            Box(
                Modifier.padding(horizontal = 24.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (banner.positive) Color(0xFFE8FBF9) else Color(0xFFFFF3F0))
                    .padding(vertical = 18.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        banner.title,
                        color = if (banner.positive) MintDark else Color(0xFFDD4B39),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    if (banner.sub.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            banner.sub,
                            color = if (banner.positive) MintDark else Color(0xFFDD4B39),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

private fun weekRangeOf(anchor: LocalDate): List<LocalDate> {
    val start = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    return (0..6).map { start.plusDays(it.toLong()) }
}

private data class Banner(val title: String, val sub: String, val positive: Boolean)

private fun bannerInfo(items: List<MedItem>): Banner {
    if (items.isEmpty()) return Banner("오늘 복약 일정이 없습니다", "", true)
    val done = items.count { it.status == IntakeStatus.DONE }
    val total = items.size
    val pct = (done * 100f / total).toInt()
    return when {
        done == total -> Banner("모든 복약을 완료했습니다", "완료율 $pct%", true)
        pct >= 60 -> Banner("좋습니다. 나머지도 잊지 마세요", "완료율 $pct%", true)
        else -> Banner("복약 주기를 지키면 리포트 정확도가 올라갑니다", "완료율 $pct%", false)
    }
}
@Composable
private fun PillRow(dot: Color, title: String, time: String, trailing: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 14.sp, color = Color(0xFF101828), lineHeight = 1.43.em)
                Text(text = time, fontSize = 12.sp, color = GrayText, lineHeight = 1.33.em)
            }
        }
        trailing()
    }
}
@Composable
private fun DoneText() {
    Text("복용 완료", color = Mint, fontSize = 12.sp)
}
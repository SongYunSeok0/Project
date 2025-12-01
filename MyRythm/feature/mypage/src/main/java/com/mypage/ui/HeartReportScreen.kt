package com.mypage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domain.model.HeartRateHistory
import com.domain.sharedvm.HeartRateVMContract
import com.shared.R
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HeartReportScreen(
    vm: HeartRateVMContract
) {
    val latestBpm by vm.latestHeartRate.collectAsState()
    val heartHistory by vm.heartHistory.collectAsState()

    LaunchedEffect(Unit) {
        vm.syncHeartHistory()
        vm.loadLatestHeartRate()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 상단 카드
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(296.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xffffe8e8), Color(0xffffd5d5))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.rate),
                        contentDescription = null,
                        alpha = 0.66f,
                        colorFilter = ColorFilter.tint(Color(0xffff6b6b)),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("현재 심박수", color = Color(0xff4a5565), fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.heartrate),
                            contentDescription = null,
                            modifier = Modifier
                                .height(90.dp)
                                .width(300.dp)
                                .alpha(0.60f),
                            colorFilter = ColorFilter.tint(Color(0xffff6b6b))
                        )

                        Text(
                            text = latestBpm?.toString() ?: "--",
                            color = Color(0xff101828),
                            fontSize = 60.sp
                        )
                    }

                    Text("bpm", color = Color(0xff4a5565), fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // 새로고침 버튼
        item {
            Button(
                onClick = {
                    vm.syncHeartHistory()
                    vm.loadLatestHeartRate()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff6ae0d9)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("새로고침", color = Color.White)
            }

            Spacer(Modifier.height(24.dp))

            Text("최근 측정 기록", fontSize = 16.sp, color = Color(0xff101828))
            Spacer(Modifier.height(12.dp))
        }

        // 기록 리스트
        if (heartHistory.isEmpty()) {
            item {
                Text(
                    text = "아직 심박수 기록이 없어요.",
                    fontSize = 14.sp,
                    color = Color(0xff667085),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            items(heartHistory) { item ->
                HeartHistoryRow(item)
            }
        }
    }
}

@Composable
private fun HeartHistoryRow(item: HeartRateHistory) {
    val (statusText, statusColor) = bpmStatus(item.bpm)
    val timeText = formatCollectedAt(item.collectedAt)

    Column(Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${item.bpm} bpm", fontSize = 16.sp, color = Color(0xFF111827))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(statusText, fontSize = 11.sp, color = statusColor)
                }
            }

            Text(
                text = timeText,
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Divider(
            color = Color(0xFFE5E7EB),
            thickness = 0.7.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

private fun formatCollectedAt(raw: String): String {
    return runCatching {
        val odt = OffsetDateTime.parse(raw)
        odt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
    }.getOrElse { raw }
}

private fun bpmStatus(bpm: Int): Pair<String, Color> {
    return when {
        bpm < 50 -> "낮음" to Color(0xFF3B82F6)
        bpm <= 90 -> "정상" to Color(0xFF16A34A)
        else -> "주의" to Color(0xFFEF4444)
    }
}

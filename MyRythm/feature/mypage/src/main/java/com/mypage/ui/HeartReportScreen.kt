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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domain.model.HeartRateHistory
import com.domain.sharedvm.HeartRateVMContract
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.theme.AppFieldHeight
import com.shared.ui.theme.componentTheme
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HeartReportScreen(
    vm: HeartRateVMContract
) {
    val latestBpm by vm.latestHeartRate.collectAsState()
    val heartHistory by vm.heartHistory.collectAsState()

    val currentHeartRateText = stringResource(R.string.currentheartrate)
    val bpmText = stringResource(R.string.bpm)
    val noBpmText = stringResource(R.string.no_bpm)
    val refreshText = stringResource(R.string.refresh)
    val recentMeasurementHeartrate = stringResource(R.string.recent_measurement_heartrate)
    val errorNoHeartHistory = stringResource(R.string.mypage_error_no_heart_history)


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
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                MaterialTheme.componentTheme.heartRateCardGradientLight,
                                MaterialTheme.componentTheme.heartRateCardGradientDark)
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
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        currentHeartRateText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

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
                                .alpha(0.60f)
                        )
                        Text(
                            text = latestBpm?.toString() ?: noBpmText,
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 60.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        bpmText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        // 새로고침 버튼
        item {
            AppButton(
                text = refreshText,
                onClick = {
                    vm.syncHeartHistory()
                    vm.loadLatestHeartRate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppFieldHeight),
                backgroundColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.onPrimary,
                textStyle = MaterialTheme.typography.labelLarge
            )

            Spacer(Modifier.height(24.dp))

            Text(
                recentMeasurementHeartrate,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
        }
        // 기록 리스트
        if (heartHistory.isEmpty()) {
            item {
                Text(
                    text = errorNoHeartHistory,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
    val bpmText = stringResource(R.string.bpm)

    Column(Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${item.bpm} $bpmText",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Divider(
            color = MaterialTheme.colorScheme.surfaceVariant,
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

@Composable
private fun bpmStatus(bpm: Int): Pair<String, Color> {
    val normalText = stringResource(R.string.normal)
    val lowText = stringResource(R.string.low)
    val warningText = stringResource(R.string.warning)

    return when {
        bpm < 50 -> lowText to MaterialTheme.componentTheme.heartRateLow
        bpm <= 90 -> normalText to MaterialTheme.componentTheme.heartRateNormal
        else -> warningText to MaterialTheme.componentTheme.heartRateWarning
    }
}

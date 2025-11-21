package com.mypage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domain.model.HeartRateHistory   // 🔥 도메인 모델 위치에 맞게 수정
import com.shared.R

@Composable
fun HeartReportScreen(
    userId: String?,
    viewModel: com.mypage.viewmodel.MyPageViewModel = hiltViewModel()
) {
    val heartDescription = stringResource(R.string.heart_description)
    val currentHeartRateText = stringResource(R.string.currentheartrate)
    val rateDescription = stringResource(R.string.rate_description)
    val bpmText = stringResource(R.string.bpm)
    val normalText = stringResource(R.string.normal)
    val arrowDescription = stringResource(R.string.arrow_description)
    val measureHeartRateText = stringResource(R.string.measureheartrate)
    val recentMeasurementHeartRateText = stringResource(R.string.recent_measurement_heartrate)

    // 🔹 ViewModel 상태 가져오기
    val latestBpm by viewModel.latestHeartRate.collectAsState()
    val heartHistory by viewModel.heartHistory.collectAsState()

    // 화면 들어올 때 한 번 로딩
    LaunchedEffect(Unit) {
        viewModel.refreshHeartData()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ==========================
        //  상단 심박수 카드
        // ==========================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(296.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xffffe8e8), Color(0xffffd5d5))
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
                    contentDescription = heartDescription,
                    alpha = 0.66f,
                    colorFilter = ColorFilter.tint(Color(0xffff6b6b)),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentHeartRateText,
                    color = Color(0xff4a5565),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.heartrate),
                        contentDescription = rateDescription,
                        modifier = Modifier
                            .height(90.dp)
                            .width(300.dp)
                            .alpha(0.60f)
                            .offset(x = 80.dp, y = -10.dp),
                        colorFilter = ColorFilter.tint(Color(0xffff6b6b))
                    )

                    Text(
                        text = latestBpm?.toString() ?: "--",
                        color = Color(0xff101828),
                        fontSize = 60.sp
                    )
                }

                Text(
                    text = bpmText,
                    color = Color(0xff4a5565),
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputChip(
                    label = {
                        Text(
                            normalText,
                            fontSize = 14.sp,
                            color = Color(0xff364153)
                        )
                    },
                    shape = RoundedCornerShape(50.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White.copy(alpha = 0.5f)
                    ),
                    selected = true,
                    onClick = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================
        //  심박수 측정 / 새로고침 버튼
        // ==========================
        Button(
            onClick = {
                viewModel.refreshHeartData()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff6ae0d9)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow),
                    contentDescription = arrowDescription,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(measureHeartRateText, color = Color.White, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================
        //  최근 측정 기록 제목
        // ==========================
        Text(
            text = recentMeasurementHeartRateText,
            fontSize = 16.sp,
            color = Color(0xff101828)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ==========================
        //  최근 측정 기록 리스트
        // ==========================
        HeartHistoryList(
            history = heartHistory,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ----------------------------------------
//  최근 측정 기록 리스트 컴포저블
// ----------------------------------------
@Composable
private fun HeartHistoryList(
    history: List<HeartRateHistory>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) {
        Text(
            text = "아직 심박수 기록이 없어요.",
            fontSize = 14.sp,
            color = Color(0xff667085),
            modifier = modifier.padding(top = 8.dp)
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .heightIn(max = 280.dp)
            .padding(top = 8.dp)
    ) {
        items(history) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.bpm} bpm",
                    fontSize = 16.sp,
                    color = Color(0xff101828)
                )
                Text(
                    text = item.collectedAt,
                    fontSize = 12.sp,
                    color = Color(0xff667085),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Divider(
                color = Color(0xffe4e7ec),
                thickness = 1.dp
            )
        }
    }
}

@Preview(widthDp = 392, heightDp = 1271)
@Composable
private fun HeartReportScreenPreview() {
    // Preview용 더미 ViewModel 쓰지 않아서 여기선 그냥 null 넘겨도 됨
    HeartReportScreen(userId = null)
}

package com.mypage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
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
    val cautionText = stringResource(R.string.caution)
    val warningText = stringResource(R.string.warning)
    val arrowDescription = stringResource(R.string.arrow_description)
    val measureHeartRateText = stringResource(R.string.measureheartrate)
    val recentMeasurementHeartRateText = stringResource(R.string.recent_measurement_heartrate)
    val latestBpm by viewModel.latestHeartRate.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLatestHeartRate()
    }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally // 전체 Column 중앙 정렬
            ) {
                // 심박수 카드
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
                        horizontalAlignment = Alignment.CenterHorizontally, // 카드 내부 중앙 정렬
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
                                    .offset(x = 80.dp, y= -10.dp),
                                colorFilter = ColorFilter.tint(Color(0xffff6b6b))
                            )

                            Text(
                                text = latestBpm?.toString() ?: "--",   // 하드코딩 심박수 데이터, 추후 실데이터 변수명 변경 필요
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
                            label = { Text(normalText, fontSize = 14.sp, color = Color(0xff364153)) },
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

                // 심박수 측정 버튼
                Button(
                    onClick = { /* TODO */ },
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

                // 최근 측정 기록 제목
                Text(
                    text = recentMeasurementHeartRateText,
                    fontSize = 16.sp,
                    color = Color(0xff101828)
                )

                // 여기에 기록 리스트 추가 가능
            }
        }





@Preview(widthDp = 392, heightDp = 1271)
@Composable
private fun HeartReportScreenPreview() {
    HeartReportScreen(userId = null)
}
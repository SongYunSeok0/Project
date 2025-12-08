package com.healthinsight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domain.model.DailyStep
import com.domain.usecase.health.DailyHeartRateUI
import com.domain.usecase.plan.MedicationDelayUI
import com.healthinsight.viewmodel.HealthInsightViewModel
import com.shared.ui.components.SimpleBarChart

@Composable
fun HealthInsightScreen(
    viewModel: HealthInsightViewModel = hiltViewModel()
) {
    val weeklySteps by viewModel.weeklySteps.collectAsState()
    val weeklyHeartRates by viewModel.weeklyHeartRates.collectAsState()
    val medicationDelays by viewModel.medicationDelays.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAll()
    }

    HealthInsightContent(
        weeklySteps = weeklySteps,
        weeklyHeartRates = weeklyHeartRates,
        medicationDelays = medicationDelays,
        isLoading = isLoading
    )
}

@Composable
private fun HealthInsightContent(
    weeklySteps: List<DailyStep>,
    weeklyHeartRates: List<DailyHeartRateUI>,
    medicationDelays: List<MedicationDelayUI>,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "건강 인사이트",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 걸음수 카드 (막대 그래프)
            StepsCard(weeklySteps)

            // 심박수 카드 (막대 그래프)
            HeartRateCard(weeklyHeartRates)

            // 복약 시간 카드 (막대 그래프)
            MedicationDelayCard(medicationDelays)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StepsCard(weeklySteps: List<DailyStep>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "최근 7일 걸음수",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (weeklySteps.isEmpty()) {
                Text(
                    text = "데이터가 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                val values = weeklySteps.map { it.steps }
                val labels = weeklySteps.map { day ->
                    if (day.date.length >= 5) {
                        day.date.takeLast(5)  // "MM-DD"
                    } else {
                        day.date
                    }
                }

                SimpleBarChart(
                    values = values,
                    labels = labels
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 통계 정보
                val avgSteps = values.average().toInt()
                val maxSteps = values.maxOrNull() ?: 0

                Text(
                    text = "평균: ${avgSteps}걸음 · 최고: ${maxSteps}걸음",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HeartRateCard(weeklyHeartRates: List<DailyHeartRateUI>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "최근 7일 평균 심박수",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (weeklyHeartRates.isEmpty()) {
                Text(
                    text = "데이터가 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                val avgValues = weeklyHeartRates.map { day ->
                    if (day.measurements.isNotEmpty()) {
                        day.measurements.average().toInt()
                    } else 0
                }

                val labels = weeklyHeartRates.map { day ->
                    if (day.date.length >= 5) {
                        day.date.takeLast(5)  // "MM-DD"
                    } else {
                        day.date
                    }
                }

                SimpleBarChart(
                    values = avgValues,
                    labels = labels
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 통계 정보
                val totalMeasurements = weeklyHeartRates.sumOf { it.measurements.size }
                val avgBpm = avgValues.filter { it > 0 }.average().toInt()

                Text(
                    text = "평균: ${avgBpm}bpm · 측정 횟수: ${totalMeasurements}회",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MedicationDelayCard(medicationDelays: List<MedicationDelayUI>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "복약 시간 준수도",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (medicationDelays.isEmpty()) {
                Text(
                    text = "데이터가 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                val delayValues = medicationDelays.map { it.delayMinutes }
                val labels = medicationDelays.map { delay ->
                    if (delay.date.length >= 5) {
                        delay.date.takeLast(5)  // "MM-DD"
                    } else {
                        delay.date
                    }
                }

                SimpleBarChart(
                    values = delayValues,
                    labels = labels
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 통계 정보
                val avgDelay = delayValues.average()
                val onTimeCount = delayValues.count { it in -5..5 }  // ±5분 이내
                val onTimeRate = (onTimeCount.toFloat() / delayValues.size * 100).toInt()

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "평균 지연: ${if (avgDelay > 0) "+%.1f".format(avgDelay) else "%.1f".format(avgDelay)}분",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "정시 복용률: $onTimeRate% ($onTimeCount/${delayValues.size}회)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// 🎨 프리뷰
// 🎨 프리뷰 - 현실적인 데이터
@Preview(showBackground = true)
@Composable
private fun HealthInsightScreenPreview() {
    MaterialTheme {
        HealthInsightContent(
            weeklySteps = listOf(
                DailyStep(date = "12-01", steps = 5200),   // 낮음
                DailyStep(date = "12-02", steps = 12800),  // 높음
                DailyStep(date = "12-03", steps = 7300),   // 보통
                DailyStep(date = "12-04", steps = 9800),   // 보통
                DailyStep(date = "12-05", steps = 15200),  // 매우 높음
                DailyStep(date = "12-06", steps = 3800),   // 매우 낮음
                DailyStep(date = "12-07", steps = 10500)   // 높음
            ),
            weeklyHeartRates = listOf(
                DailyHeartRateUI(date = "12-01", measurements = listOf(68, 72, 70)),  // 낮음
                DailyHeartRateUI(date = "12-02", measurements = listOf(75, 78, 80)),  // 높음
                DailyHeartRateUI(date = "12-03", measurements = listOf(71, 73, 72)),  // 보통
                DailyHeartRateUI(date = "12-04", measurements = listOf(65, 67, 66)),  // 낮음
                DailyHeartRateUI(date = "12-05", measurements = listOf(82, 85, 88)),  // 매우 높음
                DailyHeartRateUI(date = "12-06", measurements = listOf(70, 72, 71)),  // 보통
                DailyHeartRateUI(date = "12-07", measurements = listOf(76, 78, 77))   // 높음
            ),
            medicationDelays = listOf(
                MedicationDelayUI(
                    date = "12-01",
                    label = "혈압약",
                    scheduledTime = 0L,
                    actualTime = 0L,
                    delayMinutes = -5,    // 5분 일찍
                    isTaken = true
                ),
                MedicationDelayUI(
                    date = "12-02",
                    label = "혈압약",
                    scheduledTime = 0L,
                    actualTime = 0L,
                    delayMinutes = 25,    // 25분 지연
                    isTaken = true
                ),
                MedicationDelayUI(
                    date = "12-03",
                    label = "혈압약",
                    scheduledTime = 0L,
                    actualTime = 0L,
                    delayMinutes = 3,     // 3분 지연 (정시)
                    isTaken = true
                ),
                MedicationDelayUI(
                    date = "12-04",
                    label = "혈압약",
                    scheduledTime = 0L,
                    actualTime = 0L,
                    delayMinutes = 45,    // 45분 지연 (많이)
                    isTaken = true
                ),
                MedicationDelayUI(
                    date = "12-05",
                    label = "혈압약",
                    scheduledTime = 0L,
                    actualTime = 0L,
                    delayMinutes = -2,    // 2분 일찍 (정시)
                    isTaken = true
                ),
                MedicationDelayUI(
                    date = "12-06",
                    label = "혈압약",
                    scheduledTime = 0L,
                    actualTime = 0L,
                    delayMinutes = 12,    // 12분 지연
                    isTaken = true
                ),
                MedicationDelayUI(
                    date = "12-07",
                    label = "혈압약",
                    scheduledTime = 0L,
                    actualTime = 0L,
                    delayMinutes = 1,     // 1분 지연 (정시)
                    isTaken = true
                )
            ),
            isLoading = false
        )
    }
}
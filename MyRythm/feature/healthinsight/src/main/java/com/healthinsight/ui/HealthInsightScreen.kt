package com.healthinsight.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domain.model.DailyStep
import com.domain.usecase.health.DailyHeartRateUI
import com.domain.usecase.plan.MedicationDelayUI
import com.healthinsight.ui.components.HealthBarChart
import com.healthinsight.viewmodel.HealthInsightViewModel
import com.shared.ui.theme.componentTheme
import kotlin.math.abs

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
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "건강 인사이트",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.componentTheme.heartRateNormal
            )

            StepsCard(weeklySteps)
            HeartRateCard(weeklyHeartRates)
            MedicationDelayCard(medicationDelays)
        }
    }
}

@Composable
private fun StepsCard(weeklySteps: List<DailyStep>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.componentTheme.healthInsightCard
        ),
        border = BorderStroke(1.dp, MaterialTheme.componentTheme.mainFeatureCardBorderStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "최근 7일 걸음수",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.componentTheme.stepCard
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (weeklySteps.isEmpty()) {
                Text(
                    text = "데이터가 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.componentTheme.dividerColor,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                val values = weeklySteps.map { it.steps }
                val labels = weeklySteps.map { day ->
                    if (day.date.length >= 5) day.date.takeLast(5) else day.date
                }

                HealthBarChart(
                    values = values,
                    labels = labels,
                    barColor = MaterialTheme.componentTheme.stepCard,
                    axisColor = MaterialTheme.componentTheme.dividerColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                val avgSteps = values.average().toInt()
                val maxSteps = values.maxOrNull() ?: 0

                Text(
                    text = "평균: ${avgSteps}걸음 · 최고: ${maxSteps}걸음",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.componentTheme.dividerColor
                )
            }
        }
    }
}

@Composable
private fun HeartRateCard(weeklyHeartRates: List<DailyHeartRateUI>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.componentTheme.healthInsightCard
        ),
        border = BorderStroke(1.dp, MaterialTheme.componentTheme.mainFeatureCardBorderStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "최근 7일 평균 심박수",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.componentTheme.heartRateNormal
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (weeklyHeartRates.isEmpty()) {
                Text(
                    text = "데이터가 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.componentTheme.dividerColor,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                val avgValues = weeklyHeartRates.map { day ->
                    if (day.measurements.isNotEmpty()) day.measurements.average().toInt() else 0
                }

                val labels = weeklyHeartRates.map { day ->
                    if (day.date.length >= 5) day.date.takeLast(5) else day.date
                }

                HealthBarChart(
                    values = avgValues,
                    labels = labels,
                    barColor = MaterialTheme.componentTheme.rateCard,
                    axisColor = MaterialTheme.componentTheme.dividerColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                val totalMeasurements = weeklyHeartRates.sumOf { it.measurements.size }
                val avgBpm = avgValues.filter { it > 0 }.average().toInt()

                Text(
                    text = "평균: ${avgBpm}bpm · 측정 횟수: ${totalMeasurements}회",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.componentTheme.dividerColor
                )
            }
        }
    }
}

@Composable
private fun MedicationDelayCard(medicationDelays: List<MedicationDelayUI>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.componentTheme.healthInsightCard
        ),
        border = BorderStroke(1.dp, MaterialTheme.componentTheme.mainFeatureCardBorderStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "복약 시간 준수도",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.componentTheme.completionCaution
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (medicationDelays.isEmpty()) {
                Text(
                    text = "데이터가 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.componentTheme.dividerColor,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                val groupedByDate = medicationDelays.groupBy { it.date }
                val sortedDates = groupedByDate.keys.sorted()

                val labels = sortedDates.map { date ->
                    if (date.length >= 5) date.takeLast(5) else date
                }

                val avgDailyDelays: List<Double> = sortedDates.map { date ->
                    val dayDelays = groupedByDate[date].orEmpty().map { it.delayMinutes }
                    if (dayDelays.isEmpty()) 0.0 else dayDelays.average()
                }

                HealthBarChart(
                    values = avgDailyDelays,
                    labels = labels,
                    barColor = MaterialTheme.componentTheme.completionCaution,
                    axisColor = MaterialTheme.componentTheme.dividerColor,
                    isDelayChart = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                val allDelayMinutes = medicationDelays.map { it.delayMinutes }
                val overallAvgDelay = allDelayMinutes.average()
                val onTimeCount = allDelayMinutes.count { it in -5..5 }
                val onTimeRate =
                    (onTimeCount.toFloat() / allDelayMinutes.size.toFloat() * 100f).toInt()

                val avgOfDailyAvgDelayAbs =
                    if (avgDailyDelays.isEmpty()) 0.0
                    else avgDailyDelays.map { abs(it) }.average()

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "하루 평균 지연(절대값): %.1f분".format(avgOfDailyAvgDelayAbs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.componentTheme.dividerColor
                    )
                    Text(
                        text = "전체 평균 지연: ${
                            if (overallAvgDelay > 0)
                                "+%.1f".format(overallAvgDelay)
                            else
                                "%.1f".format(overallAvgDelay)
                        }분",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.componentTheme.dividerColor
                    )
                    Text(
                        text = "정시 복용률(회차 기준): $onTimeRate% ($onTimeCount/${allDelayMinutes.size}회)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.componentTheme.completionCaution
                    )
                }
            }
        }
    }
}

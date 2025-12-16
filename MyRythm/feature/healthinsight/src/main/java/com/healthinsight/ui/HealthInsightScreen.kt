package com.healthinsight.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.domain.model.DailyStep
import com.domain.usecase.health.DailyHeartRateUI
import com.domain.usecase.plan.MedicationDelayUI
import com.healthinsight.ui.components.HealthBarChart
import com.healthinsight.ui.components.HealthLineChart
import com.healthinsight.viewmodel.HealthInsightViewModel
import com.shared.R
import com.shared.ui.theme.AppTheme
import com.shared.ui.theme.componentTheme

@Composable
fun HealthInsightScreen(
    viewModel: HealthInsightViewModel = hiltViewModel()
) {
    AppTheme {
        val weeklySteps by viewModel.weeklySteps.collectAsStateWithLifecycle()
        val weeklyHeartRates by viewModel.weeklyHeartRates.collectAsStateWithLifecycle()
        val medicationDelays by viewModel.medicationDelays.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) {
            viewModel.loadAll()
        }

        LaunchedEffect(weeklyHeartRates) {
            println("UI received ${weeklyHeartRates.size} days of heart rate data")
            weeklyHeartRates.forEach { day ->
                println("UI: ${day.date} - ${day.measurements.size} measurements")
            }
        }

        HealthInsightContent(
            weeklySteps = weeklySteps,
            weeklyHeartRates = weeklyHeartRates,
            medicationDelays = medicationDelays,
            isLoading = isLoading,
            onInsertTestData = { viewModel.insertTestData() }
        )
    }
}

@Composable
private fun HealthInsightContent(
    weeklySteps: List<DailyStep>,
    weeklyHeartRates: List<DailyHeartRateUI>,
    medicationDelays: List<MedicationDelayUI>,
    isLoading: Boolean,
    onInsertTestData: () -> Unit
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
            // 🔥 개발용 테스트 버튼 (배포 시 제거)
            Button(
                onClick = onInsertTestData,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text("걸음수 테스트 데이터 삽입")
            }

            MedicationDelayCard(medicationDelays)
            if (weeklyHeartRates.isNotEmpty()) {
                HeartRateCard(weeklyHeartRates)
            }
            if (weeklySteps.isNotEmpty()) {
                StepsCard(weeklySteps)
            }
        }
    }
}

@Composable
private fun StepsCard(weeklySteps: List<DailyStep>) {
    val recentStepText = stringResource(R.string.recent_step)
    val noDataMessage = stringResource(R.string.healthinsight_message_no_data)
    val averageText = stringResource(R.string.average)
    val maximumText = stringResource(R.string.maximum)
    val stepText = stringResource(R.string.steps_unit)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = recentStepText,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (weeklySteps.isEmpty()) {
                Text(
                    text = noDataMessage,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                val values = weeklySteps.map { it.steps }
                val labels = weeklySteps.map { day ->
                    val parts = day.date.split("-")
                    if (parts.size >= 3) "${parts[1]}/${parts[2]}"
                    else day.date.takeLast(5)
                }

                val stepColors = listOf(
                    MaterialTheme.componentTheme.stepBarBlueColor,
                    MaterialTheme.componentTheme.stepBarBlueDarkColor,
                    MaterialTheme.componentTheme.stepBarBlueDarkerColor,
                )

                HealthBarChart(
                    values = values,
                    labels = labels,
                    barColors = stepColors,
                    axisColor = MaterialTheme.colorScheme.outline,
                    valueUnit = ""
                )

                Spacer(modifier = Modifier.height(12.dp))

                val avgSteps = values.average().toInt()
                val maxSteps = values.maxOrNull() ?: 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        averageText,
                        "${String.format("%,d", avgSteps)}$stepText",
                        MaterialTheme.componentTheme.stepBarBlueColor
                    )
                    StatItem(
                        maximumText,
                        "${String.format("%,d", maxSteps)}$stepText",
                        MaterialTheme.componentTheme.stepBarBlueDarkerColor
                    )
                }
            }
        }
    }
}

@Composable
private fun HeartRateCard(weeklyHeartRates: List<DailyHeartRateUI>) {
    val noDataMessage = stringResource(R.string.healthinsight_message_no_data)
    val heartRateTitle = stringResource(R.string.heart_rate_title)
    val averageText = stringResource(R.string.average)
    val maximumText = stringResource(R.string.maximum)
    val minimumText = stringResource(R.string.minimum)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = heartRateTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (weeklyHeartRates.isEmpty()) {
                Text(
                    text = noDataMessage,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                val measurements = weeklyHeartRates.map { day ->
                    day.measurements
                }

                val labels = weeklyHeartRates.map { day ->
                    val parts = day.date.split("-")
                    if (parts.size >= 3) "${parts[1]}/${parts[2]}"
                    else day.date.takeLast(5)
                }

                HealthLineChart(
                    measurements = measurements,
                    labels = labels,
                    lineColor = MaterialTheme.componentTheme.heartRateLineColor,
                    valueUnit = ""
                )

                Spacer(modifier = Modifier.height(12.dp))

                val allMeasurements = weeklyHeartRates.flatMap { it.measurements }
                val avgBpm = if (allMeasurements.isNotEmpty()) {
                    allMeasurements.average().toInt()
                } else 0
                val maxBpm = allMeasurements.maxOrNull() ?: 0
                val minBpm = allMeasurements.minOrNull() ?: 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(averageText, "${avgBpm}bpm", MaterialTheme.componentTheme.heartRateAverageColor)
                    StatItem(maximumText, "${maxBpm}bpm", MaterialTheme.componentTheme.heartRateMaxColor)
                    StatItem(minimumText, "${minBpm}bpm", MaterialTheme.componentTheme.heartRateMinColor)
                }
            }
        }
    }
}

@Composable
private fun MedicationDelayCard(medicationDelays: List<MedicationDelayUI>) {
    val noDataMessage = stringResource(R.string.healthinsight_message_no_data)
    val mediComplianceTitle = stringResource(R.string.medi_compliance_title)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = mediComplianceTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (medicationDelays.isEmpty()) {
                Text(
                    text = noDataMessage,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                val groupedByMed = medicationDelays.groupBy { it.label }

                groupedByMed.entries.forEachIndexed { index, (medName, delays) ->
                    MedicationChart(
                        medName = medName,
                        delays = delays
                    )

                    if (index < groupedByMed.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationChart(
    medName: String,
    delays: List<MedicationDelayUI>
) {
    val countPerDayText = stringResource(R.string.count_per_day)
    val averageDelayText = stringResource(R.string.average_delay)
    val minuteText = stringResource(R.string.minute)
    val ontimeRateText = stringResource(R.string.ontime_rate)

    val minimumText = stringResource(R.string.minimum)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔹 $medName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color =  MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${delays.size}$countPerDayText",
                style = MaterialTheme.typography.bodySmall,
                color =  MaterialTheme.colorScheme.surfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val groupedByDate = delays.groupBy { it.date }
        val sortedDates = groupedByDate.keys.sorted()

        val labels = sortedDates.map { date ->
            val parts = date.split("-")
            if (parts.size >= 3) "${parts[1]}/${parts[2]}"
            else date.takeLast(5)
        }

        val avgDailyDelays: List<Double> = sortedDates.map { date ->
            val dayDelays = groupedByDate[date].orEmpty().map { it.delayMinutes }
            if (dayDelays.isEmpty()) 0.0 else dayDelays.average()
        }

        val avgDelay = delays.map { it.delayMinutes }.average()
        val medicationColors = when {
            avgDelay <= 5 -> listOf(
                MaterialTheme.componentTheme.medicationDelayGood1Color,
                MaterialTheme.componentTheme.medicationDelayGood2Color,
                MaterialTheme.componentTheme.medicationDelayGood3Color
            )
            avgDelay <= 15 -> listOf(
                MaterialTheme.componentTheme.medicationDelayNormal1Color,
                MaterialTheme.componentTheme.medicationDelayNormal2Color,
                MaterialTheme.componentTheme.medicationDelayNormal3Color
            )
            else -> listOf(
                MaterialTheme.componentTheme.medicationDelayBad1Color,
                MaterialTheme.componentTheme.medicationDelayBad2Color,
                MaterialTheme.componentTheme.medicationDelayBad3Color
            )
        }

        HealthBarChart(
            values = avgDailyDelays,
            labels = labels,
            barColors = medicationColors,
            axisColor = MaterialTheme.colorScheme.onSurfaceVariant,
            isDelayChart = true,
            valueUnit = minuteText
        )

        Spacer(modifier = Modifier.height(8.dp))

        val onTimeCount = delays.count { it.delayMinutes in -5..5 }
        val onTimeRate = (onTimeCount.toFloat() / delays.size * 100).toInt()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = averageDelayText,
                    style = MaterialTheme.typography.labelSmall,
                    color =  MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "${if (avgDelay > 0) "+" else ""}%.1f분".format(avgDelay),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color =  MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = ontimeRateText,
                    style = MaterialTheme.typography.labelSmall,
                    color =  MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "$onTimeRate%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        onTimeRate >= 90 -> MaterialTheme.componentTheme.onTimeRateGoodColor
                        onTimeRate >= 70 -> MaterialTheme.componentTheme.onTimeRateNormalColor
                        else -> MaterialTheme.componentTheme.onTimeRateBadColor
                    }
                )
            }
        }

        Divider(
            modifier = Modifier.padding(top = 16.dp),
            color = Color(0xFFE0E0E0),
            thickness = 1.dp
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = color
        )
    }
}
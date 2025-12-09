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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.domain.model.DailyStep
import com.domain.usecase.health.DailyHeartRateUI
import com.domain.usecase.plan.MedicationDelayUI
import com.healthinsight.ui.components.HealthBarChart
import com.healthinsight.ui.components.HealthLineChart
import com.healthinsight.viewmodel.HealthInsightViewModel

@Composable
fun HealthInsightScreen(
    viewModel: HealthInsightViewModel = hiltViewModel()
) {
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
        }
    }
}

@Composable
private fun StepsCard(weeklySteps: List<DailyStep>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
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
                text = "🚶 최근 7일 걸음수",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                    val parts = day.date.split("-")
                    if (parts.size >= 3) "${parts[1]}/${parts[2]}"
                    else day.date.takeLast(5)
                }

                val stepColors = listOf(
                    Color(0xFF2196F3),
                    Color(0xFF1E88E5),
                    Color(0xFF1976D2),
                )

                HealthBarChart(
                    values = values,
                    labels = labels,
                    barColors = stepColors,
                    axisColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    valueUnit = ""
                )

                Spacer(modifier = Modifier.height(12.dp))

                val avgSteps = values.average().toInt()
                val maxSteps = values.maxOrNull() ?: 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("평균", "${String.format("%,d", avgSteps)}걸음", Color(0xFF2196F3))
                    StatItem("최고", "${String.format("%,d", maxSteps)}걸음", Color(0xFF1976D2))
                }
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
            containerColor = Color.White
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
                text = "❤️ 최근 7일 심박수 추이",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (weeklyHeartRates.isEmpty()) {
                Text(
                    text = "데이터가 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
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
                    lineColor = Color(0xFFE91E63),
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
                    StatItem("평균", "${avgBpm}bpm", Color(0xFFE91E63))
                    StatItem("최고", "${maxBpm}bpm", Color(0xFFD81B60))
                    StatItem("최저", "${minBpm}bpm", Color(0xFFC2185B))
                }
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
            containerColor = Color.White
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
                text = "💊 복약 시간 준수도",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (medicationDelays.isEmpty()) {
                Text(
                    text = "데이터가 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
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
                color = Color.Black
            )

            Text(
                text = "${delays.size}회",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
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
                Color(0xFF66BB6A),
                Color(0xFF81C784),
                Color(0xFF4CAF50)
            )
            avgDelay <= 15 -> listOf(
                Color(0xFFFFB74D),
                Color(0xFFFF9800),
                Color(0xFFFFA726)
            )
            else -> listOf(
                Color(0xFFEF5350),
                Color(0xFFE57373),
                Color(0xFFD32F2F)
            )
        }

        HealthBarChart(
            values = avgDailyDelays,
            labels = labels,
            barColors = medicationColors,
            axisColor = MaterialTheme.colorScheme.onSurfaceVariant,
            isDelayChart = true,
            valueUnit = "분"
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
                    text = "평균 지연",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = "${if (avgDelay > 0) "+" else ""}%.1f분".format(avgDelay),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "정시 복용률",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = "$onTimeRate%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        onTimeRate >= 90 -> Color(0xFF66BB6A)
                        onTimeRate >= 70 -> Color(0xFFFF9800)
                        else -> Color(0xFFEF5350)
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
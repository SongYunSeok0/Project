package com.healthinsight.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.domain.usecase.health.DailyHeartRateUI
import com.healthinsight.ui.components.StatItem
import com.shared.R
import com.shared.ui.components.InsightCard
import com.shared.ui.theme.componentTheme

@Composable
fun HeartRateCard(
    weeklyHeartRates: List<DailyHeartRateUI>
) {
    val noDataMessage = stringResource(R.string.healthinsight_message_no_data)
    val heartRateTitle = stringResource(R.string.heart_rate_title)
    val averageText = stringResource(R.string.average)
    val maximumText = stringResource(R.string.maximum)
    val minimumText = stringResource(R.string.minimum)
    val bpmText = stringResource(R.string.bpm)

    InsightCard(
        title = heartRateTitle,
        isEmpty = weeklyHeartRates.isEmpty(),
        emptyMessage = noDataMessage
    ) {
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
            StatItem(averageText, "${avgBpm}$bpmText", MaterialTheme.componentTheme.heartRateAverageColor)
            StatItem(maximumText, "${maxBpm}$bpmText", MaterialTheme.componentTheme.heartRateMaxColor)
            StatItem(minimumText, "${minBpm}$bpmText", MaterialTheme.componentTheme.heartRateMinColor)
        }
    }
}
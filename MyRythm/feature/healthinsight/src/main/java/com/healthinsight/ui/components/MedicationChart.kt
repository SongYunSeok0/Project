package com.healthinsight.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.domain.usecase.plan.MedicationDelayUI
import com.shared.R
import com.shared.ui.theme.componentTheme
import kotlin.collections.orEmpty

@Composable
fun MedicationChart(
    medName: String,
    delays: List<MedicationDelayUI>
) {
    val percentText = stringResource(R.string.percent_suffix)
    val countPerDayText = stringResource(R.string.count_per_day)
    val averageDelayText = stringResource(R.string.average_delay)
    val minuteText = stringResource(R.string.minute)
    val ontimeRateText = stringResource(R.string.ontime_rate)
    val categoryPointer = stringResource(R.string.category_pointer)

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
                text = "$categoryPointer$medName",
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
                    //text = "${if (avgDelay > 0) "+" else ""}%.1f분".format(avgDelay),
                    text = "${if (avgDelay > 0) "+" else ""}${"%.1f".format(avgDelay)}$minuteText",
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
                    text = "$onTimeRate$percentText",
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
            color = MaterialTheme.componentTheme.healthInsightDividerColor,
            thickness = 1.dp
        )
    }
}
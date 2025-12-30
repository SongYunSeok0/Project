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
import com.domain.model.DailyStep
import com.healthinsight.ui.components.StatItem
import com.shared.R
import com.shared.ui.components.InsightCard
import com.shared.ui.theme.componentTheme

@Composable
fun StepsCard(weeklySteps: List<DailyStep>) {
    val recentStepText = stringResource(R.string.recent_step)
    val noDataMessage = stringResource(R.string.healthinsight_message_no_data)
    val averageText = stringResource(R.string.average)
    val maximumText = stringResource(R.string.maximum)
    val stepText = stringResource(R.string.steps_unit)

    InsightCard (
        title = recentStepText,
        isEmpty = weeklySteps.isEmpty(),
        emptyMessage = noDataMessage
    ) {
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

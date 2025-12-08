package com.healthinsight.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domain.model.DailyStep
import com.shared.R
import com.shared.ui.components.SimpleBarChart

@Composable
fun HealthInsightScreen(
    weeklySteps: List<DailyStep>
) {
    val healthInsightText = stringResource(R.string.healthinsight)
    val recentStepText = stringResource(R.string.recent_step)

    val values = weeklySteps.map { it.steps }
    val labels = weeklySteps.map { day ->
        if (day.date.length >= 5) {
            day.date.takeLast(5)
        } else {
            day.date
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = healthInsightText,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = recentStepText,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        SimpleBarChart(
            values = values,
            labels = labels
        )
    }
}

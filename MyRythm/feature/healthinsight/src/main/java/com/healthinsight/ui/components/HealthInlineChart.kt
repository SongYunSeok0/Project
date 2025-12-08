package com.healthinsight.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

@Composable
fun HealthLineChart(
    measurements: List<List<Int>>, // 각 날짜별 측정값 리스트 (하루 3번)
    labels: List<String>, // 날짜 라벨
    modifier: Modifier = Modifier,
    lineColor: Color,
    valueUnit: String = ""
) {
    if (measurements.isEmpty()) return

    // 🔥 디버깅
    println("HealthLineChart: ${measurements.size} days, labels: ${labels.size}")
    labels.forEachIndexed { index, label ->
        println("  [$index] $label: ${measurements.getOrNull(index)?.size ?: 0} measurements")
    }

    // 모든 측정값을 하나의 선으로 연결
    // x축: 0, 1, 2 (day1 측정), 3, 4, 5 (day2 측정), ...
    val allEntries = mutableListOf<Pair<Float, Float>>()

    measurements.forEachIndexed { dayIndex, dayMeasurements ->
        dayMeasurements.forEachIndexed { measurementIndex, value ->
            val xPosition = (dayIndex * 3 + measurementIndex).toFloat()
            allEntries.add(xPosition to value.toFloat())
        }
    }

    val chartEntryModelProducer = remember(measurements) {
        ChartEntryModelProducer(
            allEntries.map { (x, y) -> entryOf(x, y) }
        )
    }

    val allValues = measurements.flatten().map { it.toFloat() }
    val minY = allValues.minOrNull() ?: 0f
    val maxY = allValues.maxOrNull() ?: 0f
    val range = maxY - minY
    val padding = if (range < 10f) 5f else range * 0.2f

    val axisValuesOverrider = remember(minY, maxY) {
        AxisValuesOverrider.fixed(
            minY = (minY - padding).coerceAtLeast(0f),
            maxY = maxY + padding
        )
    }

    ProvideChartStyle {
        Chart(
            chart = lineChart(
                axisValuesOverrider = axisValuesOverrider,
                spacing = 24.dp
            ),
            chartModelProducer = chartEntryModelProducer,
            startAxis = rememberStartAxis(
                label = textComponent(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                valueFormatter = { value, _ ->
                    if (valueUnit.isNotEmpty()) {
                        "%.0f%s".format(value, valueUnit)
                    } else {
                        "%.0f".format(value)
                    }
                },
                itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 5),
                axis = null,
                guideline = lineComponent(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    thickness = 1.dp
                )
            ),
            bottomAxis = rememberBottomAxis(
                label = textComponent(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                valueFormatter = { value, _ ->
                    // 각 날짜의 중간 지점(측정 2번째)에만 날짜 표시
                    val dayIndex = (value.toInt() / 3)
                    val measurementIndex = value.toInt() % 3
                    if (measurementIndex == 1 && dayIndex < labels.size) {
                        labels[dayIndex]
                    } else {
                        ""
                    }
                },
                axis = lineComponent(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                ),
                guideline = null
            ),
            modifier = modifier
                .fillMaxWidth()
                .height(230.dp)
                .padding(vertical = 8.dp, horizontal = 4.dp)
        )
    }
}
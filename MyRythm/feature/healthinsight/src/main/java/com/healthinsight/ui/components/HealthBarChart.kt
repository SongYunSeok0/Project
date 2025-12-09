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
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlin.math.abs
import kotlin.math.max

@Composable
fun HealthBarChart(
    values: List<Number>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColors: List<Color>,
    axisColor: Color,
    isDelayChart: Boolean = false,
    valueUnit: String = "" // 🆕 단위 추가
) {
    if (values.isEmpty()) return

    val floatValues = values.map { it.toFloat() }

    val chartEntryModelProducer = remember(values) {
        ChartEntryModelProducer(
            floatValues.mapIndexed { index, value ->
                entryOf(index.toFloat(), value)
            }
        )
    }

    val minY = floatValues.minOrNull() ?: 0f
    val maxY = floatValues.maxOrNull() ?: 0f

    val axisValuesOverrider = remember(minY, maxY, isDelayChart) {
        if (isDelayChart) {
            val maxAbs = max(abs(minY), abs(maxY)).coerceAtLeast(5f)
            val padding = maxAbs * 0.2f
            AxisValuesOverrider.fixed(
                minY = -maxAbs - padding,
                maxY = maxAbs + padding
            )
        } else {
            val range = maxY - minY
            val padding = if (range < 10f) 5f else range * 0.2f
            AxisValuesOverrider.fixed(
                minY = (minY - padding).coerceAtLeast(0f),
                maxY = maxY + padding
            )
        }
    }

    val columnComponents = barColors.map { color ->
        lineComponent(
            color = color,
            thickness = 18.dp,
            shape = Shapes.roundedCornerShape(
                topLeftPercent = 30,
                topRightPercent = 30
            )
        )
    }

    val zeroLineComponent = lineComponent(
        color = MaterialTheme.colorScheme.outline,
        thickness = 2.5.dp
    )

    val decorations = if (isDelayChart) {
        listOf(
            ThresholdLine(
                thresholdValue = 0f,
                lineComponent = zeroLineComponent
            )
        )
    } else emptyList()

    ProvideChartStyle {
        Chart(
            chart = columnChart(
                columns = columnComponents,
                axisValuesOverrider = axisValuesOverrider,
                decorations = decorations,
                spacing = 28.dp
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
                    labels.getOrNull(value.toInt()) ?: ""
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
package com.healthinsight.ui.components

import android.graphics.Typeface
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlin.math.abs
import kotlin.math.max

// 🔥 모노톤 공용 컬러
private val BarGray = Color(0xFF444444)        // 막대 색
private val AxisText = Color(0xFF222222)       // 축 텍스트
private val GridLine = Color(0xFFDDDDDD)       // 그리드 라인
private val ZeroLine = Color(0xFF000000)       // 0 기준선 강조

@Composable
fun HealthBarChart(
    values: List<Number>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = BarGray,   // 기본: 회색 막대
    axisColor: Color = AxisText, // 기본: 진한 회색 글자
    isDelayChart: Boolean = false
) {
    if (values.isEmpty()) return

    val floatValues = values.map { it.toFloat() }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false

                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)
                setTouchEnabled(false)

                // 🔹 차트 여백 (조금만)
                setExtraOffsets(8f, 4f, 8f, 12f)

                // 🔹 X축
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    textColor = axisColor.toArgb()
                    textSize = 11f
                }

                // 오른쪽 축 제거
                axisRight.isEnabled = false

                // 🔹 왼쪽 축 (Y축)
                axisLeft.apply {
                    textColor = axisColor.toArgb()
                    textSize = 11f
                    setDrawAxisLine(true)
                    axisLineColor = axisColor.toArgb()
                    setDrawGridLines(true)
                    gridColor = GridLine.toArgb()
                }

                setNoDataText("")
            }
        },
        update = { chart ->
            val entries = floatValues.mapIndexed { index, v ->
                BarEntry(index.toFloat(), v)
            }

            val dataSet = BarDataSet(entries, "").apply {
                color = barColor.toArgb()            // 회색 막대
                valueTextColor = axisColor.toArgb()
                valueTextSize = 10f
                valueTypeface = Typeface.DEFAULT_BOLD
                setDrawValues(false)
                highLightAlpha = 0
            }

            chart.data = BarData(dataSet).apply {
                barWidth = 0.5f
            }

            // X축 라벨
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

            // --- Y축 범위 조정 ---
            val minY = floatValues.minOrNull() ?: 0f
            val maxY = floatValues.maxOrNull() ?: 0f

            chart.axisLeft.apply {
                if (isDelayChart) {
                    // 0 기준 위/아래 대칭
                    val maxAbs = max(abs(minY), abs(maxY)).coerceAtLeast(1f)
                    axisMinimum = -maxAbs - 1f
                    axisMaximum =  maxAbs + 1f

                    // 0 기준선 검정색
                    setDrawZeroLine(true)
                    zeroLineColor = ZeroLine.toArgb()
                    zeroLineWidth = 1.8f
                } else {
                    // 일반 차트는 패딩만
                    val padding = (maxY - minY).coerceAtLeast(10f) * 0.1f
                    axisMinimum = minY - padding
                    axisMaximum = maxY + padding
                    setDrawZeroLine(false)
                }
            }

            chart.invalidate()
        }
    )
}

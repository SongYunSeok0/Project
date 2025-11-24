package com.shared.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun SimpleBarChart(
    values: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (values.isEmpty() || labels.isEmpty() || values.size != labels.size) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "데이터가 없습니다",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }

    val maxValue = max(values.maxOrNull() ?: 0, 1)

    // 🔥 DrawScope에서 불러오면 crash → 미리 색 추출
    val barColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val barCount = values.size
            val barAreaWidth = size.width / barCount
            val barWidth = barAreaWidth * 0.4f
            val maxBarHeight = size.height

            values.forEachIndexed { index, v ->
                val ratio = v.toFloat() / maxValue.toFloat()
                val barHeight = maxBarHeight * ratio

                val centerX = barAreaWidth * index + barAreaWidth / 2f
                val left = centerX - barWidth / 2f
                val top = maxBarHeight - barHeight

                drawRoundRect(
                    color = barColor,   // ← 안전
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

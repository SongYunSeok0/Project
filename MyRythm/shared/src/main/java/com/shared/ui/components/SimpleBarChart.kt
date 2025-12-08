package com.shared.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

@Composable
fun SimpleBarChart(
    values: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF6DD5ED), // 이미지처럼 하늘색
    showValues: Boolean = true,
    showGrid: Boolean = true,
    animated: Boolean = true
) {
    if (values.isEmpty() || labels.isEmpty() || values.size != labels.size) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
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

    val maxValue = (values.maxOrNull() ?: 1)
    val minValue = (values.minOrNull() ?: 0)

    // 🎨 그라데이션 색상
    val gradientColors = listOf(
        barColor.copy(alpha = 0.8f),
        barColor.copy(alpha = 0.4f)
    )

    // ✨ 애니메이션
    val animatedProgress by animateFloatAsState(
        targetValue = if (animated) 1f else 1f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "bar_animation"
    )

    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            val barCount = values.size
            val spacing = size.width * 0.08f // 막대 사이 간격
            val totalSpacing = spacing * (barCount - 1)
            val availableWidth = size.width - totalSpacing
            val barWidth = availableWidth / barCount * 0.85f // 막대 너비

            val maxBarHeight = size.height - 40f // 값 표시 공간 확보

            // 🔲 그리드 라인 (옵션)
            if (showGrid) {
                val gridColor = Color.Gray.copy(alpha = 0.2f)
                for (i in 0..4) {
                    val y = (maxBarHeight / 4) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }
            }

            values.forEachIndexed { index, value ->
                val ratio = if (maxValue > 0) {
                    value.toFloat() / maxValue.toFloat()
                } else {
                    0f
                }

                val barHeight = max(maxBarHeight * ratio * animatedProgress, 8f)

                val xPosition = (barWidth + spacing) * index
                val left = xPosition
                val top = maxBarHeight - barHeight

                // 🎨 그라데이션 막대
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = gradientColors,
                        startY = top,
                        endY = maxBarHeight
                    ),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // 📊 값 표시
                if (showValues && animatedProgress > 0.5f) {
                    val textLayoutResult = textMeasurer.measure(
                        text = value.toString(),
                        style = textStyle
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            left + (barWidth - textLayoutResult.size.width) / 2,
                            top - textLayoutResult.size.height - 4f
                        )
                    )
                }
            }
        }

        // 🏷️ 레이블
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
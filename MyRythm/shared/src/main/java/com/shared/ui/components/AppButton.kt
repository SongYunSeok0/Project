package com.shared.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 선택 가능한 버튼 (AuthPrimaryButton 스타일과 최대한 유사)
 *
 * @param text 버튼 텍스트
 * @param selected 선택 상태
 * @param onClick 클릭 이벤트
 * @param modifier Modifier
 * @param height 버튼 높이
 * @param selectedColor 선택 상태 배경 색상
 * @param unselectedColor 비선택 상태 배경 색상
 * @param selectedBorderColor 선택 상태 테두리 색상
 * @param unselectedBorderColor 비선택 상태 테두리 색상
 * @param selectedTextColor 선택 상태 텍스트 색상
 * @param unselectedTextColor 비선택 상태 텍스트 색상
 * @param useClickEffect 클릭 시 색상 변화 적용 여부
 */

// 선택 시 민트, 미선택 시 회색인 셀렉터블 버튼
@Composable
fun AppSelectableButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),

    // 색상 최소 옵션만 유지
    selectedBackground: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    unselectedBackground: Color = Color.White,
    selectedTextColor: Color = MaterialTheme.colorScheme.primary,
    unselectedTextColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.primary, // 선택 상태 테두리
    useClickEffect: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 클릭 시 배경 색상 결정
    val backgroundColor = when {
        useClickEffect && isPressed -> selectedBackground.copy(alpha = 0.3f)
        selected -> selectedBackground
        else -> unselectedBackground
    }
    val actualBorderColor = if (selected) borderColor else unselectedTextColor
    val textColor = actualBorderColor

    Surface(
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.5.dp, actualBorderColor),
        modifier = modifier
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .height(height)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AppButton(
    text: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    cornerRadius: Dp = 12.dp,                // ⭐ 추가됨
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = Color.White,
    useClickEffect: Boolean = true,
    content: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val finalBackground = when {
        useClickEffect && isPressed -> backgroundColor.copy(alpha = 0.7f)
        else -> backgroundColor
    }

    Surface(
        color = finalBackground,
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier
            .height(height)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (content != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    content()

                    if (text.isNotEmpty()) Spacer(Modifier.width(6.dp))

                    if (text.isNotEmpty()) {
                        Text(text, color = textColor, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                Text(
                    text = text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
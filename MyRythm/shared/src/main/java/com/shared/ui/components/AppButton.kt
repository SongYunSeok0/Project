package com.shared.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
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
@Composable
fun AppSelectableButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 75.dp,
    selectedColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    unselectedColor: Color = Color.White,
    selectedBorderColor: Color = MaterialTheme.colorScheme.primary,
    unselectedBorderColor: Color = Color.Gray,
    selectedTextColor: Color = MaterialTheme.colorScheme.primary,
    unselectedTextColor: Color =  Color.Gray,
    useClickEffect: Boolean = true
) {
    val themeSelectedColor = selectedColor ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    val themeSelectedBorder = selectedBorderColor ?: MaterialTheme.colorScheme.primary
    val themeSelectedText = selectedTextColor ?: MaterialTheme.colorScheme.primary

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 클릭 시 배경 색상 결정
    val backgroundColor = when {
        useClickEffect && isPressed -> selectedColor.copy(alpha = 0.3f)
        selected -> selectedColor
        else -> unselectedColor
    }

    val borderColor = if (selected) selectedBorderColor else unselectedBorderColor
    val textColor = if (selected) selectedTextColor else unselectedTextColor

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = BorderStroke(1.5.dp, borderColor),
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

package com.shared.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shared.ui.theme.AppTheme

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
    selectedBackground: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    unselectedBackground: Color = MaterialTheme.colorScheme.surface,
    selectedTextColor: Color = MaterialTheme.colorScheme.primary,
    unselectedTextColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    useClickEffect: Boolean = true,
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

// 일반적인 버튼. 사이즈는 조절 필요, 클릭하는 순간 컬러 바뀌는 이펙트만 있음,
// isCircle = true 클릭 시 원형버튼 / 기본 사각 버튼
/*@Composable
fun AppButton(
    text: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp? = null,
    shape: Shape = MaterialTheme.shapes.small,
    isCircle: Boolean = false,
    backgroundColor: Color? = null,
    enabled: Boolean = true,
    textColor: Color? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    useClickEffect: Boolean = true,
    content: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val defaultBackground = backgroundColor ?: MaterialTheme.colorScheme.primary
    val defaultTextColor = textColor ?: MaterialTheme.colorScheme.onPrimary

    val finalBackground = if (useClickEffect && isPressed) {
        defaultBackground.copy(alpha = 0.7f)
    } else {
        defaultBackground
    }

    val finalShape =
        if (isCircle) RoundedCornerShape(50)
        else shape

    Surface(
        color = finalBackground,
        shape = finalShape,
        modifier = modifier
            .then(
                if (height != null) Modifier.height(height) else Modifier
            )
            .then(
                if (width != null) Modifier.width(width) else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            if (content != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    content()

                    if (text.isNotEmpty()) Spacer(Modifier.width(6.dp))

                    if (text.isNotEmpty()) {
                        Text(text, color = defaultTextColor, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                Text(
                    text = text,
                    color = defaultTextColor,
                    style = textStyle
                )
            }
        }
    }
}*/
// 일반적인 버튼. 사이즈는 조절 필요, 클릭하는 순간 컬러 바뀌는 이펙트만 있음,
// isCircle = true 클릭 시 원형버튼 / 기본 사각 버튼
@Composable
fun AppButton(
    text: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp? = null,
    shape: Shape = MaterialTheme.shapes.small,
    isCircle: Boolean = false,
    backgroundColor: Color? = null,
    enabled: Boolean = true,
    textColor: Color? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    useClickEffect: Boolean = true,
    isOutlined: Boolean = false,
    outlineColor: Color = MaterialTheme.colorScheme.primary,
    content: (@Composable () -> Unit)? = null
) {
    AppTheme {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        val defaultBackground = backgroundColor ?: MaterialTheme.colorScheme.primary
        val defaultTextColor = textColor ?: MaterialTheme.colorScheme.onPrimary

        // 🔹 아웃라인이면 배경색은 배경색(흰색)
        val baseBackground =
            if (isOutlined) MaterialTheme.colorScheme.background else defaultBackground

        val finalBackground = when {
            !enabled -> baseBackground.copy(alpha = 0.38f) // 비활성화 시 투명도 조절
            useClickEffect && isPressed -> defaultBackground.copy(alpha = 0.7f) // 클릭 시 효과
            else -> baseBackground
        }

        val finalShape =
            if (isCircle) RoundedCornerShape(50)
            else shape

        val finalTextColor = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            isOutlined -> textColor ?: outlineColor
            else -> defaultTextColor
        }

        Surface(
            color = finalBackground,
            shape = finalShape,
            border = if (isOutlined) BorderStroke(1.5.dp, outlineColor) else null,
            modifier = modifier
                .then(if (height != null) Modifier.height(height) else Modifier)
                .then(if (width != null) Modifier.width(width) else Modifier)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() }
        ) {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                if (content != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        content()
                        if (text.isNotEmpty()) Spacer(Modifier.width(6.dp))
                        if (text.isNotEmpty()) {
                            Text(
                                text,
                                color = finalTextColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    Text(
                        text = text,
                        color = finalTextColor,
                        style = textStyle
                    )
                }
            }
        }
    }
}


// chip 버튼
@Composable
fun AppTagButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = true,
    useFilterChipStyle: Boolean = false,    // true 설정 시 필터칩 적용 + 셀렉터블버튼 스타일
    leadingIcon: Painter? = null,   // 이건 왼쪽아이콘이고 필요 시 trailingIcon: 오른쪽아이콘
    contentDescription: String? = null,
    backgroundColor: Color? = null,
    alpha: Float = 1f,
    textColor: Color? = null,
    isCircle: Boolean? = null,
    useClickEffect: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val defaultBackground = backgroundColor ?: MaterialTheme.colorScheme.primary.copy(alpha = alpha)
    val defaultTextColor = textColor ?: MaterialTheme.colorScheme.onSurface

    // 필터칩 스타일 true면 셀렉터블버튼처럼 디자인 적용
    val finalBackground = when {
        useFilterChipStyle -> {
            if (selected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }
        useClickEffect && isPressed -> {
            defaultBackground.copy(alpha = 0.7f)
        }
        else -> defaultBackground
    }

    val finalBorderColor = when {
        useFilterChipStyle -> {
            if (selected)
                MaterialTheme.colorScheme.primary          // 선택 시 민트 테두리
            else
                MaterialTheme.colorScheme.surfaceVariant   // 미선택 시 회색 테두리
        }
        else -> Color.Transparent
    }
    // 글씨 컬러 (필터칩+셀렉터블 룩에서 선택/미선택 구분)
    val finalTextColor = when {
        useFilterChipStyle -> {
            if (selected)
                MaterialTheme.colorScheme.onSurface          // 선택 글씨색
            else
                MaterialTheme.colorScheme.outline.copy(0.7f)   // 미선택 글씨색
        }
        else -> defaultTextColor
    }

    // 원형/타원형/사각형 shape
    val finalShape = when (isCircle) {
        null -> MaterialTheme.shapes.medium     // 기본 사각형
        true -> RoundedCornerShape(50)          // 완전 원형
        false -> CircleShape                    // 기본 타원형(Pill)
    }

    InputChip(
        label = {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = finalTextColor,
            )
        },
        leadingIcon = leadingIcon?.let { icon ->
            {
                Image(
                    painter = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        shape = finalShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = finalBackground,
            selectedContainerColor = finalBackground,
            labelColor = finalTextColor,
            selectedLabelColor = finalTextColor
        ),
        border = if (useFilterChipStyle)
            BorderStroke(1.5.dp, finalBorderColor)
        else null,

        selected = selected,
        onClick = onClick,
        modifier = modifier.height(32.dp),
        interactionSource = interactionSource
    )
}

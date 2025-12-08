package com.shared.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shared.ui.theme.AppFieldHeight
import com.shared.ui.theme.authTheme
import com.shared.ui.theme.loginTheme

// 메인액션버튼 (회원가입, 로그인 등) : 파란바탕-하얀글자
/**
 * 인증 화면의 공통 메인 버튼 (Filled Button)
 *
 * @param text 버튼 텍스트
 * @param onClick 클릭 이벤트
 * @param modifier Modifier
 * @param enabled 활성화 여부
 * @param useClickEffect 클릭 시 투명도 효과 적용 여부 (기본: true)
 */
@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    useClickEffect: Boolean = true
) {
    val buttonShape = MaterialTheme.shapes.medium
    val textStyle = MaterialTheme.typography.labelLarge
    val buttonColor = MaterialTheme.loginTheme.loginPrimaryButton
    val textColor = MaterialTheme.loginTheme.loginOnPrimary
    val clickColor = buttonColor.copy(alpha = 0.5f)

    // 클릭 상태 감지
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    // 클릭 효과 적용 여부에 따라 색상 결정
    val finalButtonColor = if (useClickEffect && isPressed) clickColor else buttonColor

    Button(
        onClick = onClick,
        modifier = modifier
            .height(AppFieldHeight),
        enabled = enabled,
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = finalButtonColor,
            disabledContainerColor = buttonColor.copy(alpha = 0.5f),
            contentColor = textColor,
            disabledContentColor = textColor.copy(alpha = 0.7f)
        ),
        interactionSource = interactionSource
    ) {
        Text(text = text, style = textStyle)
    }
}

// 서브액션버튼 (뒤로가기, 취소 등) : 흰색바탕-파란글자
/**
 * 인증 화면의 공통 서브 버튼 (Outlined Button)
 *
 * @param text 버튼 텍스트
 * @param onClick 클릭 이벤트
 * @param modifier Modifier
 * @param enabled 활성화 여부
 */
@Composable
fun AuthSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val buttonShape = MaterialTheme.shapes.medium
    val textStyle = MaterialTheme.typography.labelLarge
    val buttonColor =MaterialTheme.loginTheme.loginOnPrimary
    val textColor = MaterialTheme.loginTheme.loginPrimaryButton

    Button(
        onClick = onClick,
        modifier = modifier.height(AppFieldHeight),
        enabled = enabled,
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            disabledContainerColor = buttonColor.copy(alpha = 0.5f),
            contentColor = textColor,
            disabledContentColor = textColor.copy(alpha = 0.7f)
        )
    ) {
        Text(text = text, style = textStyle)
    }
}


/**
 * 작은 액션 버튼 (전송, 인증 등)
 *
 * @param text 버튼 텍스트
 * @param onClick 클릭 이벤트
 * @param modifier Modifier
 * @param enabled 활성화 여부
 * @param useClickEffect 클릭 시 투명도 효과 적용 여부
 */
@Composable
fun AuthActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    useClickEffect: Boolean = true
) {
    val buttonShape = MaterialTheme.shapes.medium
    val textStyle = MaterialTheme.typography.labelLarge
    val buttonColor = MaterialTheme.loginTheme.loginPrimaryButton
    val textColor = MaterialTheme.loginTheme.loginOnPrimary
    val clickColor = buttonColor.copy(alpha = 0.5f)

    // 클릭 상태 감지
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 클릭 효과 적용
    val finalButtonColor = if (useClickEffect && isPressed) clickColor else buttonColor

    Button(
        onClick = onClick,
        modifier = modifier
            .height(AppFieldHeight),
        enabled = enabled,
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = finalButtonColor,
            disabledContainerColor = buttonColor.copy(alpha = 0.5f),
            contentColor = textColor,
            disabledContentColor = textColor.copy(alpha = 0.7f)
        ),
        interactionSource = interactionSource
    ) {
        Text(text = text, style = textStyle)
    }
}

@Composable
fun AuthTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.loginTheme.loginTertiary,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    )
}
package com.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ui.theme.authTheme
import com.ui.theme.loginTheme

/**
 * 인증 화면의 공통 메인 버튼 (Filled Button)
 *
 * @param text 버튼 텍스트
 * @param onClick 클릭 이벤트
 * @param modifier Modifier
 * @param enabled 활성화 여부
 * @param useLoginTheme true면 loginTheme, false면 authTheme 사용
 * @param useClickEffect 클릭 시 투명도 효과 적용 여부 (기본: true)
 */
@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    useLoginTheme: Boolean = true,
    useClickEffect: Boolean = true
) {
    val buttonShape = MaterialTheme.shapes.medium
    val textStyle = MaterialTheme.typography.labelLarge

    // 테마에서 색상 가져오기
    val buttonColor = if (useLoginTheme) {
        MaterialTheme.loginTheme.loginPrimaryButton
    } else {
        MaterialTheme.authTheme.authPrimaryButton
    }

    val textColor = if (useLoginTheme) {
        MaterialTheme.loginTheme.loginOnPrimary
    } else {
        MaterialTheme.authTheme.authOnPrimary
    }

    val clickColor = if (useLoginTheme) {
        buttonColor.copy(alpha = 0.5f)  // loginTheme은 별도 clickColor 없음
    } else {
        MaterialTheme.authTheme.authPrimaryButtonClick
    }

    // 클릭 상태 감지
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 클릭 효과 적용 여부에 따라 색상 결정
    val finalButtonColor = if (useClickEffect && isPressed) clickColor else buttonColor

    Button(
        onClick = onClick,
        modifier = modifier,
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
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * 인증 화면의 공통 서브 버튼 (Outlined Button)
 *
 * @param text 버튼 텍스트
 * @param onClick 클릭 이벤트
 * @param modifier Modifier
 * @param enabled 활성화 여부
 * @param useLoginTheme true면 loginTheme, false면 authTheme 사용
 */
@Composable
fun AuthSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    useLoginTheme: Boolean = true
) {
    val buttonShape = MaterialTheme.shapes.medium
    val textStyle = MaterialTheme.typography.labelLarge

    // 테마에서 색상 가져오기
    val buttonColor = if (useLoginTheme) {
        MaterialTheme.loginTheme.loginSecondrayButton
    } else {
        MaterialTheme.authTheme.authSecondrayButton
    }

    val textColor = if (useLoginTheme) {
        MaterialTheme.loginTheme.loginOnSecondray
    } else {
        MaterialTheme.authTheme.authOnSecondray
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = buttonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = buttonColor,
            disabledContainerColor = buttonColor.copy(alpha = 0.5f),
            contentColor = textColor,
            disabledContentColor = textColor.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, textColor)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * 작은 액션 버튼 (전송, 인증 등)
 *
 * @param text 버튼 텍스트
 * @param onClick 클릭 이벤트
 * @param modifier Modifier
 * @param enabled 활성화 여부
 * @param useLoginTheme true면 loginTheme, false면 authTheme 사용
 * @param useClickEffect 클릭 시 투명도 효과 적용 여부
 */
@Composable
fun AuthActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    useLoginTheme: Boolean = true,
    useClickEffect: Boolean = true
) {
    val buttonShape = MaterialTheme.shapes.medium
    val textStyle = MaterialTheme.typography.labelLarge

    // 테마에서 색상 가져오기
    val buttonColor = if (useLoginTheme) {
        MaterialTheme.loginTheme.loginPrimaryButton
    } else {
        MaterialTheme.authTheme.authPrimaryButton
    }

    val textColor = if (useLoginTheme) {
        MaterialTheme.loginTheme.loginOnPrimary
    } else {
        MaterialTheme.authTheme.authOnPrimary
    }

    val clickColor = if (useLoginTheme) {
        buttonColor.copy(alpha = 0.5f)
    } else {
        MaterialTheme.authTheme.authPrimaryButtonClick
    }

    // 클릭 상태 감지
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 클릭 효과 적용
    val finalButtonColor = if (useClickEffect && isPressed) clickColor else buttonColor

    Button(
        onClick = onClick,
        modifier = modifier,
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
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

// 사용 예시:
/*
// 로그인 화면 - 메인 버튼
AuthPrimaryButton(
    text = "로그인",
    onClick = { onLogin() },
    modifier = Modifier.fillMaxWidth().height(56.dp),
    useLoginTheme = true
)

// 로그인 화면 - 서브 버튼
AuthSecondaryButton(
    text = "회원가입",
    onClick = { onSignUp() },
    modifier = Modifier.fillMaxWidth().height(52.dp),
    useLoginTheme = true
)

// 회원가입 화면 - 메인 버튼 (클릭 효과 있음)
AuthPrimaryButton(
    text = "회원 가입 완료",
    onClick = { onComplete() },
    modifier = Modifier.fillMaxWidth().height(62.dp),
    useLoginTheme = false,  // authTheme 사용
    useClickEffect = true
)

// 전송 버튼 (작은 버튼, 클릭 효과 있음)
AuthActionButton(
    text = "전송",
    onClick = { onSend() },
    modifier = Modifier.height(56.dp),
    enabled = phone.isNotBlank(),
    useLoginTheme = false,
    useClickEffect = true
)
*/
package com.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.ui.theme.AuthFieldHeight // Dimension.kt에서 가져옴
import com.ui.theme.ShadowElevationDefault
import com.ui.theme.authTheme

/**
 * 로그인/회원가입 등 인증 화면의 공통 입력 필드 컴포넌트 (AuthTheme 색상으로 통합)
 *
 * 이 컴포넌트는 앱 전체에서 일관된 디자인을 위해 AuthTheme의 색상을 사용하도록 고정되었습니다.
 *
 * @param value 현재 입력된 값
 * @param onValueChange 값 변경 콜백
 * @param hint Placeholder 텍스트
 * @param isPassword 비밀번호 필드 여부 (true일 경우 표시/숨김 아이콘 표시)
 * @param imeAction 키보드 액션 (Next, Done 등)
 * @param keyboardActions 키보드 액션 처리
 * @param modifier Modifier
 */
@Composable
fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    // 비밀번호 표시/숨김 상태
    var passwordVisible by remember { mutableStateOf(false) }

    // 모든 입력 필드는 MaterialTheme.authTheme의 색상을 사용하도록 고정 (통합)
    val authColors = MaterialTheme.authTheme

    val surfaceColor = authColors.authSurface
    val hintColor = authColors.authOnFieldHint
    val textColor = authColors.authOnSurface
    val accentColor = authColors.authPrimaryButton

    // Shape와 Typography는 공통 (Theme.kt에서 MaterialTheme에 제공됨)
    // extraSmall은 Shape.kt에서 AuthFieldShape(10.dp)로 매핑되어 있습니다.
    val fieldShape = MaterialTheme.shapes.extraSmall
    val textStyle = MaterialTheme.typography.bodyLarge

    // OutlinedTextField는 Material 3의 표준 입력 필드입니다.
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = hint,
                color = hintColor,
                style = textStyle
            )
        },
        singleLine = true,
        shape = fieldShape,
        modifier = modifier
            .height(AuthFieldHeight) // Dimension.kt의 AuthFieldHeight 사용
            .shadow(elevation = ShadowElevationDefault, shape = fieldShape),
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Filled.VisibilityOff
                        else
                            Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보기",
                        tint = accentColor
                    )
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = keyboardActions,
        textStyle = textStyle,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = surfaceColor,
            unfocusedContainerColor = surfaceColor,
            focusedBorderColor = accentColor, // OutlinedTextField의 테두리 색상
            unfocusedBorderColor = hintColor.copy(alpha = 0.5f),
            cursorColor = accentColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor
        )
    )
}
/* 사용 예시 (화면 테마와 상관없이 입력 필드는 동일):
@Composable
fun LoginScreen() { // 이 화면은 MaterialTheme.loginTheme을 사용하여 배경색 등이 다를 수 있음
    var id by remember { mutableStateOf("") }
    AuthInputField(
        value = id,
        onValueChange = { id = it },
        hint = "아이디"
        // useLoginTheme을 전달할 필요 없음
    )
}
*/



/* 1030 19:17 주석처리


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ui.theme.AuthFieldHeight
import com.ui.theme.AuthFieldShape
import com.ui.theme.AuthFieldWidth
import com.ui.theme.Colors
import com.ui.theme.ShadowElevationDefault
import com.ui.theme.authTheme

/**
 * 이름, 이메일, 비밀번호 등 인증 화면의 공통 입력 필드 스타일을 정의합니다.
 * (실제 입력 기능은 포함하지 않고 Placeholder 스타일만 재현합니다.)
 *
 * @param hint Placeholder 텍스트
 */

@Composable
fun AuthInputField(
    hint: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            shape = AuthFieldShape,
            color = Colors.AuthPrimaryButton,
            modifier = Modifier
                .requiredWidth(AuthFieldWidth)
                .requiredHeight(AuthFieldHeight)
                .shadow(elevation = ShadowElevationDefault, shape = AuthFieldShape)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
            ) {
                // 실제 TextField 대신 Placeholder 텍스트만 표시
                Text(
                    text = hint,
                    color = Colors.AuthOnPrimary,
                    fontSize = 15.sp,
                    letterSpacing = 0.9.sp
                )
            }
        }
    }
}
 */
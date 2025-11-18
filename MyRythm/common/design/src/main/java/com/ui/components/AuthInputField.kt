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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.common.design.R
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
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    // 비밀번호 표시/숨김 상태
    var passwordVisible by remember { mutableStateOf(false) }
    val authColors = MaterialTheme.authTheme
    val surfaceColor = authColors.authSurface
    val hintColor = authColors.authOnFieldHint
    val textColor = authColors.authOnSurface
    val accentColor = authColors.authPrimaryButton
    val fieldShape = MaterialTheme.shapes.extraSmall
    val textStyle = MaterialTheme.typography.bodyLarge

    val passwordShow = stringResource(R.string.auth_password_show)
    val passwordHide = stringResource(R.string.auth_password_hide)

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
                        contentDescription = if (passwordVisible) passwordHide else passwordShow,
                        tint = accentColor
                    )
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(imeAction = imeAction,keyboardType = keyboardType),
        keyboardActions = keyboardActions,
        enabled = enabled,
        textStyle = textStyle,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = surfaceColor,
            unfocusedContainerColor = surfaceColor,
            focusedBorderColor = accentColor,
            unfocusedBorderColor = hintColor.copy(alpha = 0.5f),
            cursorColor = accentColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor
        )
    )
}

/**
 * 성별 선택 드롭다운 컴포넌트 (입력 필드 스타일)
 *
 * @param value 현재 선택된 값 ("M", "F", "")
 * @param onValueChange 값 변경 콜백 (M 또는 F 반환)
 * @param hint Placeholder 텍스트
 * @param modifier Modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthGenderDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String = stringResource(R.string.auth_gender),
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // AuthTheme 색상 사용 (AuthInputField와 동일)
    val authColors = MaterialTheme.authTheme
    val surfaceColor = authColors.authSurface
    val hintColor = authColors.authOnFieldHint
    val textColor = authColors.authOnSurface
    val accentColor = authColors.authPrimaryButton
    val fieldShape = MaterialTheme.shapes.extraSmall
    val textStyle = MaterialTheme.typography.bodyLarge

    val maleText = stringResource(R.string.auth_male)
    val femaleText = stringResource(R.string.auth_female)

    // 표시할 텍스트
    val displayText = when (value) {
        "M" -> maleText
        "F" -> femaleText
        else -> ""
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            placeholder = {
                Text(
                    text = hint,
                    color = hintColor,
                    style = textStyle
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier
                .menuAnchor()
                .height(AuthFieldHeight)
                .shadow(elevation = ShadowElevationDefault, shape = fieldShape),
            shape = fieldShape,
            textStyle = textStyle,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = surfaceColor,
                unfocusedContainerColor = surfaceColor,
                focusedBorderColor = accentColor,
                unfocusedBorderColor = hintColor.copy(alpha = 0.5f),
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(maleText, style = textStyle) },
                onClick = {
                    onValueChange("M")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(femaleText, style = textStyle) },
                onClick = {
                    onValueChange("F")
                    expanded = false
                }
            )
        }
    }
}
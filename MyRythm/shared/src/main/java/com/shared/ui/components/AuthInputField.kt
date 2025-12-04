package com.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.shared.ui.theme.ShadowElevationDefault
import com.shared.ui.theme.authTheme
import com.shared.R


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
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    trailingContent: @Composable (() -> Unit)? = null
) {
    // 비밀번호 표시/숨김 상태
    var passwordVisible by remember { mutableStateOf(false) }

    // 모든 입력 필드는 MaterialTheme.authTheme의 색상을 사용하도록 고정 (통합)
    val authColors = MaterialTheme.authTheme

    val surfaceColor = authColors.authSurface   //하얀
    val hintColor = authColors.authOnFieldHint  //블랙40
    val textColor = authColors.authOnSurface    //검정
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
        shape = shape,
        modifier = modifier,
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        trailingIcon = {
            when {
                isPassword -> {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Filled.VisibilityOff
                            else
                                Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) passwordHide else passwordShow,
                            tint = hintColor
                        )
                    }
                }

                trailingContent != null -> {
                    trailingContent()
                }

                else -> {}
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = imeAction,keyboardType = keyboardType),
        keyboardActions = keyboardActions,
        enabled = enabled,
        textStyle = textStyle,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = surfaceColor,
            unfocusedContainerColor = surfaceColor,
            focusedBorderColor = hintColor,
            unfocusedBorderColor = surfaceColor,
            cursorColor = textColor
        )
    )
}

@Composable
fun AuthSectionTitle(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthGenderDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String = stringResource(R.string.gender),
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

    val maleText = stringResource(R.string.male)
    val femaleText = stringResource(R.string.female)

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
                .shadow(
                    elevation = ShadowElevationDefault,
                    shape = fieldShape
                ),
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
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(authColors.authSurface)
        ) {
            Box(
                modifier = Modifier.background(authColors.authSurface)
            ) {
                Column {
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
    }
}
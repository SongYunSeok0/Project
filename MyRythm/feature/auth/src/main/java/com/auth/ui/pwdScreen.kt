package com.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shared.R
import com.shared.ui.components.AuthInputField
import com.shared.ui.components.AuthLogoHeader
import com.shared.ui.components.AuthPrimaryButton
import com.shared.ui.components.AuthSecondaryButton
import com.shared.ui.components.AuthActionButton
import com.shared.ui.theme.authTheme
import com.shared.ui.theme.defaultFontFamily

/**
 * 비밀번호 찾기/인증 화면 (반응형 레이아웃 적용)
 *
 * 상/하단 Spacer에 weight(1f)를 적용하여 핵심 콘텐츠 블록을 수직 중앙 정렬합니다.
 */
@Composable
fun PwdScreen(
    modifier: Modifier = Modifier,
    onSendCode: (phone: String) -> Unit = {},
    onConfirm: (phone: String, code: String) -> Unit = { _, _ -> },
    onBackToLogin: () -> Unit = {}
) {
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    // 문자열 리소스화 선언
    val phoneNumberPlaceholderText = stringResource(R.string.phone_number_placeholder)
    val sendText = stringResource(R.string.send)
    val sentText = stringResource(R.string.sent)
    val verificationCodeText = stringResource(R.string.verification_code)
    val comfirmText = stringResource(R.string.confirm)
    val backtologinText = stringResource(R.string.auth_backtologin)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.authTheme.authBackground)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(50.dp))

            AuthLogoHeader(textLogoResId = R.drawable.auth_myrhythm)

            Spacer(Modifier.height(10.dp))

            // 휴대폰 번호 + 전송 버튼 -> 이메일로 변경 필요, 확인하면 비번을바꿔서저장하는필드넣거나
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuthInputField(
                    value = phone,
                    onValueChange = { phone = it },
                    hint = phoneNumberPlaceholderText,
                    modifier = Modifier.weight(1f),
                )

                Spacer(Modifier.width(8.dp))

                AuthActionButton(
                    text = if (sent) sentText else sendText,
                    onClick = {
                        sent = true
                        onSendCode(phone)
                    },
                    enabled = !sent && phone.isNotBlank(),
                    modifier = Modifier
                        .widthIn(min = 90.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // 인증번호 입력
            AuthInputField(
                value = code,
                onValueChange = { code = it },
                hint = verificationCodeText,
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Done
            )

            Spacer(Modifier.height(58.dp))

            AuthPrimaryButton(
                text = comfirmText,
                onClick = { onConfirm(phone, code) },
                modifier = Modifier
                    .fillMaxWidth(),
                useClickEffect = true
            )

            Spacer(Modifier.height(14.dp))

            AuthSecondaryButton(
                text = backtologinText,
                onClick = onBackToLogin,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(30.dp))
        }
    }
}
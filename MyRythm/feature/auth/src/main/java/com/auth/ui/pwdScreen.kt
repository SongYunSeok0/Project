package com.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.common.design.R
import com.ui.components.AuthInputField
import com.ui.components.AuthLogoHeader
import com.ui.components.AuthPrimaryButton
import com.ui.components.AuthSecondaryButton
import com.ui.components.AuthActionButton
import com.ui.theme.authTheme
import com.ui.theme.defaultFontFamily
import com.ui.theme.loginTheme

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.authTheme.authBackground)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.authTheme.authBackground)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(50.dp))

            // 로고
            AuthLogoHeader(textLogoResId = R.drawable.auth_myrhythm)

            Spacer(Modifier.height(10.dp))

            // 휴대폰 번호 + 전송 버튼
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuthInputField(
                    value = phone,
                    onValueChange = { phone = it },
                    hint = "휴대폰 번호",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next
                )

                Spacer(Modifier.width(8.dp))

                AuthActionButton(
                    text = if (sent) "전송됨" else "전송",
                    onClick = {
                        sent = true
                        onSendCode(phone)
                    },
                    enabled = !sent && phone.isNotBlank(),
                    modifier = Modifier
                        .height(56.dp)
                        .widthIn(min = 90.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // 인증번호 입력
            AuthInputField(
                value = code,
                onValueChange = { code = it },
                hint = "인증번호",
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Done
            )

            Spacer(Modifier.height(58.dp))

            AuthPrimaryButton(
                text = "확인",
                onClick = { onConfirm(phone, code) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                useLoginTheme = true,
                useClickEffect = true
            )

            Spacer(Modifier.height(14.dp))

            AuthSecondaryButton(
                text = "로그인으로",
                onClick = onBackToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                useLoginTheme = true
            )

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPwd() {
    MaterialTheme(
        typography = MaterialTheme.typography.copy(
            labelLarge = TextStyle(
                fontFamily = defaultFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            ),
            // 입력 필드와 본문 글씨
            bodyLarge = TextStyle(
                fontFamily = defaultFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            ),
            // 안내메시지 등 작은 글씨
            bodySmall = TextStyle(
                fontFamily = defaultFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp
            )
        )
    ) {
        PwdScreen()
    }
}

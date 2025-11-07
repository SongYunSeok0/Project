package com.auth

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
    onConfirm: (code: String) -> Unit = {},
    onGoLogin: () -> Unit = {}
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
                .background(Color(0xffb5e5e1))
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.7f))

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

            // 1107 추가 필요 - 뷰모델과 연계 + 확인 누를 경우 유저정보 중 비번 재설정창 이동 필요,
            // 뷰 새로 만들어야하고 pwd뷰모델은 아직 없음, 유저정보에 폰번있으니 그걸로 회원받아서비번만수정하게
            AuthPrimaryButton(
                text = "확인",
                onClick = { onConfirm(code) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                useLoginTheme = true,
                useClickEffect = true
            )

            Spacer(Modifier.height(14.dp))

            AuthSecondaryButton(
                text = "로그인으로",
                onClick = onGoLogin,
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


/* 1030 21:50 주석2
package com.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.common.design.R
import com.ui.components.AuthInputField
import com.ui.components.AuthLogoHeader
import com.ui.components.AuthPrimaryButton
import com.ui.components.AuthSecondaryButton
import com.ui.components.AuthActionButton

@Composable
fun PwdScreen(
    modifier: Modifier = Modifier,
    onSendCode: (phone: String) -> Unit = {},
    onConfirm: (code: String) -> Unit = {},
    onGoLogin: () -> Unit = {}
) {
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xffb5e5e1))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(50.dp)) // 로그인 스크린 로고 위 여백과 동일

        // 로고 + 텍스트 로고
        AuthLogoHeader(textLogoResId = R.drawable.auth_myrhythm)

        // 1. 텍스트 로고와 휴대폰 번호 입력 필드 사이 간격 (로그인 스크린과 동일: 30.dp)
        Spacer(Modifier.height(30.dp))

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

        Spacer(Modifier.height(12.dp)) // 입력 필드 간격 로그인 스크린과 동일

        // 인증번호 입력
        AuthInputField(
            value = code,
            onValueChange = { code = it },
            hint = "인증번호",
            modifier = Modifier.fillMaxWidth(),
            imeAction = ImeAction.Done
        )

        // 2. 인증번호 필드와 '확인' 버튼 사이 간격:
        // 로그인 스크린의 (8.dp + 텍스트 링크 + 18.dp) 간격을 텍스트 링크 없이 동일하게 맞추기 위해 26.dp 사용
        Spacer(Modifier.height(26.dp))

        // 확인 버튼
        AuthPrimaryButton(
            text = "확인",
            onClick = { onConfirm(code) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            useLoginTheme = true,
            useClickEffect = true
        )

        Spacer(Modifier.height(14.dp)) // 로그인 스크린 버튼 간격과 동일

        // 로그인으로 이동 버튼
        AuthSecondaryButton(
            text = "로그인으로",
            onClick = onGoLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            useLoginTheme = true
        )

        Spacer(Modifier.height(120.dp)) // 하단 여백
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPwd() {
    PwdScreen()
}
*/


/*
*/
/* 1030 19:40 주석처리
package com.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.common.design.R

@Composable
fun PwdScreen(
    modifier: Modifier = Modifier,
    onSendCode: (phone: String) -> Unit = {},
    onConfirm: (code: String) -> Unit = {},
    onGoLogin: () -> Unit = {}
) {
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xffb5e5e1))
            .padding(horizontal = 24.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "logo",
                modifier = Modifier.size(180.dp).clip(CircleShape)
            )
            Spacer(Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.login_myrhythm),
                contentDescription = "title",
                modifier = Modifier.width(320.dp).height(96.dp)
            )

            Spacer(Modifier.height(40.dp))

            // 휴대폰 번호 + 전송 버튼
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("휴대폰 번호") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF6AC0E0),
                        unfocusedIndicatorColor = Color.LightGray,
                        cursorColor = Color(0xFF6AC0E0),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { onSendCode(phone) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(56.dp)
                ) { Text("전송",
                    fontSize = 18.sp
                ) }
            }

            Spacer(Modifier.height(12.dp))

            // 인증번호 입력
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                placeholder = { Text("인증번호") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color(0xFF6AC0E0),
                    unfocusedIndicatorColor = Color.LightGray,
                    cursorColor = Color(0xFF6AC0E0),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(Modifier.height(16.dp))

            // 확인 버튼
            Button(
                onClick = { onConfirm(code) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6AC0E0))
            ) { Text("확인",
                color = Color.White,
                fontSize = 18.sp) }

            Spacer(Modifier.height(12.dp))

            // 로그인으로
            OutlinedButton(
                onClick = onGoLogin,
                enabled = true, // 항상 활성화
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF6AC0E0)
                )
            ) {
                Text("로그인으로", color = Color(0xFF6AC0E0))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPwd() {
    PwdScreen()
}
*/

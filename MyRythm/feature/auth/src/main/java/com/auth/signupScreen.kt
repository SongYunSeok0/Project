package com.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.common.design.R
// 공통 입력 필드 및 버튼 컴포넌트를 임포트합니다.
import com.ui.components.*
import com.ui.theme.AppTypography
import com.ui.theme.AuthBackground
import com.ui.theme.AuthOnPrimary
import com.ui.theme.Primary
import com.ui.theme.defaultFontFamily

@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    onSendCode: (phone: String) -> Unit = {},
    onVerify: (code: String) -> Unit = {},
    onComplete: () -> Unit = {},
    onWriteLater: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    // 입력 상태
    var name by remember { mutableStateOf("") }
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    // 인증 상태
    var sent by remember { mutableStateOf(false) }
    var verified by remember { mutableStateOf(false) }

    Scaffold(modifier = modifier.fillMaxSize()) { inner ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(AuthBackground)
                .padding(horizontal = 24.dp, vertical = 30.dp)
                .verticalScroll(scrollState)
        ) {
            AuthLogoIcon()
            Spacer(Modifier.height(24.dp))

            AuthInputField(
                value = name,
                onValueChange = { name = it },
                hint = "이름",
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next
            )

            Spacer(Modifier.height(16.dp))

            AuthInputField(
                value = id,
                onValueChange = { id = it },
                hint = "아이디",
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next
            )

            Spacer(Modifier.height(16.dp))

            AuthInputField(
                value = password,
                onValueChange = { password = it },
                hint = "비밀번호",
                isPassword = true, // 비밀번호 마스킹 및 토글 적용
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next
            )

            Spacer(Modifier.height(24.dp))

            Text("생년월일",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )

            // 생년월일 (weight를 사용한 반응형 분할)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AuthInputField(value = year, onValueChange = { year = it }, hint = "1995", modifier = Modifier.weight(1.5f))
                AuthInputField(value = month, onValueChange = { month = it }, hint = "1", modifier = Modifier.weight(1f))
                AuthInputField(value = day, onValueChange = { day = it }, hint = "1", modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            // 키/몸무게 필드 (weight를 사용한 반응형 분할)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                AuthInputField(value = height, onValueChange = { height = it }, hint = "키(cm)", modifier = Modifier.weight(1f))
                AuthInputField(value = weight, onValueChange = { weight = it }, hint = "몸무게(kg)", modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            Text("전화번호 인증 *",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )

            // 전화번호 + 전송 버튼
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AuthInputField(
                    value = phone,
                    onValueChange = { phone = it },
                    hint = "010-1111-1111",
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

            // 인증번호 + 인증 버튼
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AuthInputField(
                    value = code,
                    onValueChange = { code = it },
                    hint = "인증번호",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Done
                )
                Spacer(Modifier.width(8.dp))
                AuthSecondaryButton(
                    text = "인증",
                    onClick = {
                        verified = true
                        onVerify(code)
                    },
                    enabled = sent && code.isNotBlank(),
                    modifier = Modifier.height(56.dp).widthIn(min = 90.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // 회원 가입 완료 버튼 (메인)
            AuthPrimaryButton(
                text = "회원 가입 완료",
                onClick = { onComplete() },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )

            Spacer(Modifier.height(16.dp))

            // 나중에 작성하기 링크
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onWriteLater() }
                    .padding(vertical = 8.dp)
            ) {
                Text("나중에 작성하기",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall,
                    )
                Spacer(Modifier.width(8.dp))
                Text("(일부 기능 제한)",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun SignupScreenPreview() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Primary
        ),
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
        SignupScreen()
    }
}



/* 1030 19:40 주석처리
package com.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.common.design.R
import com.ui.theme.AuthBackground
import com.ui.theme.AuthOnPrimary
import com.ui.theme.AuthOnSecondray
import com.ui.theme.AuthSecondrayButton

@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    onSendCode: (phone: String) -> Unit = {},
    onVerify: (code: String) -> Unit = {},
    onComplete: () -> Unit = {},          // ← 완료 시 로그인으로 이동 처리
    onWriteLater: () -> Unit = {}
) {
    // 입력 상태
    var name by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    // 인증 상태
    var sent by remember { mutableStateOf(false) }
    var verified by remember { mutableStateOf(false) }

    val tfColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        focusedIndicatorColor = AuthSecondrayButton,
        unfocusedIndicatorColor = Color.LightGray,
        cursorColor = AuthSecondrayButton,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black
    )

    Scaffold(modifier = modifier.fillMaxSize()) { inner ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(AuthBackground)
                .padding(horizontal = 24.dp, vertical = 30.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "logo",
                modifier = Modifier.size(120.dp).clip(CircleShape)
            )
            Spacer(Modifier.height(24.dp))

            // 이름
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                placeholder = { Text("이름", color = AuthOnPrimary.copy(alpha = .6f)) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = tfColors
            )

            Spacer(Modifier.height(24.dp))

            Text("생년월일", color = AuthOnPrimary, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())

            // 생년/월/일
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = year, onValueChange = { year = it },
                    placeholder = { Text("1995") },
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.5f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = tfColors
                )
                OutlinedTextField(
                    value = month, onValueChange = { month = it },
                    placeholder = { Text("1") },
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = tfColors
                )
                OutlinedTextField(
                    value = day, onValueChange = { day = it },
                    placeholder = { Text("1") },
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = tfColors
                )
            }

            Spacer(Modifier.height(24.dp))

            // 키 / 몸무게
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = height, onValueChange = { height = it },
                    placeholder = { Text("키(cm)") },
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = tfColors
                )
                OutlinedTextField(
                    value = weight, onValueChange = { weight = it },
                    placeholder = { Text("몸무게(kg)") },
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = tfColors
                )
            }

            Spacer(Modifier.height(24.dp))

            Text("전화번호 인증 *", color = AuthOnPrimary, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())

            // 전화번호 + 전송
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    placeholder = { Text("010-1111-1111") },
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    colors = tfColors
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        sent = true
                        onSendCode(phone)
                    },
                    enabled = !sent && phone.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AuthSecondrayButton, disabledContainerColor = AuthSecondrayButton.copy(alpha = .5f))
                ) { Text(if (sent) "전송됨" else "전송", color = AuthOnSecondray) }
            }

            Spacer(Modifier.height(12.dp))

            // 인증번호 + 인증
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = code, onValueChange = { code = it },
                    placeholder = { Text("인증번호") },
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = tfColors
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        verified = true
                        onVerify(code)
                    },
                    enabled = sent && code.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AuthSecondrayButton, disabledContainerColor = AuthSecondrayButton.copy(alpha = .5f))
                ) { Text("인증", color = AuthOnSecondray) }
            }

            Spacer(Modifier.height(24.dp))

            // 프로필 설정 완료
            Button(
                onClick = { onComplete() },          // ← 로그인으로 이동 연결
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuthSecondrayButton,
                    disabledContainerColor = AuthSecondrayButton.copy(alpha = .5f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(62.dp)
            ) { Text("회원 가입 완료", color = AuthOnSecondray, fontSize = 14.sp) }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onWriteLater() }.padding(vertical = 8.dp)
            ) {
                Text("나중에 작성하기", color = Color.Black, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text("(일부 기능 제한)", color = Color(0xFF7CC8E4), fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignupScreenPreview() {
    SignupScreen()
}
*/

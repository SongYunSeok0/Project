package com.auth.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.auth.data.model.UserSignupRequest
import com.auth.viewmodel.SignupViewModel
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
    viewModel: SignupViewModel = viewModel(),
    onSendCode: (phone: String) -> Unit = {},
    onVerify: (code: String) -> Unit = {},
    onSignupComplete: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
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
                onClick = {
                    val user = UserSignupRequest(
                        id = id,
                        password = password,
                        name = name,
                        birth_date = "$year-$month-$day",
                        gender = "gender", // 성별 (예: "male", "female", "unknown")
                        phone = phone
                    )
                    viewModel.signup(user) { success, message ->
                        if (success) {
                            onSignupComplete()
                        } else {
                            Log.e("SignupScreen", "회원가입 실패: $message")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )

            Spacer(Modifier.height(16.dp))

            // 나중에 작성하기 링크
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBackToLogin }
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
<<<<<<<< HEAD:MyRythm/feature/auth/src/main/java/com/auth/signupScreen.kt
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
========
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
>>>>>>>> Seok:MyRythm/feature/auth/src/main/java/com/auth/ui/signupScreen.kt
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auth.viewmodel.AuthViewModel
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
========
private val SecondaryBtnDisabled = AuthSecondrayButton.copy(alpha = 0.5f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onSignupComplete: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    var id by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var birthYear by rememberSaveable { mutableStateOf("") }
    var birthMonth by rememberSaveable { mutableStateOf("") }
    var birthDay by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }

    var isPhoneVerificationSent by rememberSaveable { mutableStateOf(false) }
    var isVerificationCompleted by rememberSaveable { mutableStateOf(false) }

    val ui = viewModel.state.collectAsState().value
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg ->
            snackbar.showSnackbar(msg)
            if (msg.contains("회원가입 성공")) onSignupComplete()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AuthBackground,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { innerPadding ->
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


            // BalooThambi 는 LoginScreen.kt에 정의된 것을 사용
            Text(
                text = "My Rhythm",
                color = Color(0xff5db0a8),
                fontSize = 65.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BalooThambi
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                placeholder = { Text("이름", color = AuthOnPrimary.copy(alpha = .6f)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = tfColors
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("이름") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "생년월일",
                color = AuthOnPrimary,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = birthYear, onValueChange = { birthYear = it },
                    label = { Text("YYYY") }, modifier = Modifier.weight(1.5f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = birthMonth, onValueChange = { birthMonth = it },
                    label = { Text("MM") }, modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = birthDay, onValueChange = { birthDay = it },
                    label = { Text("DD") }, modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
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
========
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = height, onValueChange = { height = it },
                    label = { Text("키 (cm)") }, modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = weight, onValueChange = { weight = it },
                    label = { Text("몸무게 (kg)") }, modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            Spacer(Modifier.height(24.dp))

<<<<<<<< HEAD:MyRythm/feature/auth/src/main/java/com/auth/signupScreen.kt
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
========
            Text(
                text = "전화번호 인증 *",
                color = AuthOnPrimary,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("전화번호") }, modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { isPhoneVerificationSent = true },
                    enabled = !isPhoneVerificationSent,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPhoneVerificationSent) SecondaryBtnDisabled else AuthSecondrayButton,
                        contentColor = AuthOnSecondray
                    )
                ) { Text(if (isPhoneVerificationSent) "전송됨" else "전송") }
>>>>>>>> Seok:MyRythm/feature/auth/src/main/java/com/auth/ui/signupScreen.kt
            }

            Spacer(Modifier.height(12.dp))

<<<<<<<< HEAD:MyRythm/feature/auth/src/main/java/com/auth/signupScreen.kt
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
========
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = code, onValueChange = { code = it },
                    label = { Text("인증번호") }, modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { isVerificationCompleted = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuthSecondrayButton,
                        contentColor = AuthOnSecondray
                    )
                ) { Text("인증") }
>>>>>>>> Seok:MyRythm/feature/auth/src/main/java/com/auth/ui/signupScreen.kt
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (id.isBlank() || password.isBlank() || name.isBlank() ||
                        birthYear.isBlank() || birthMonth.isBlank() || birthDay.isBlank() ||
                        phone.isBlank()
                    ) {
                        viewModel.emitInfo("필수 항목을 모두 입력하세요")
                        return@Button
                    }
                    if (!isVerificationCompleted) {
                        viewModel.emitInfo("전화번호 인증을 완료하세요")
                        return@Button
                    }

                    val birthDate = "${birthYear}-${birthMonth.padStart(2, '0')}-${birthDay.padStart(2, '0')}"
                    val req = UserSignupRequest(
                        id = id,
                        password = password,
                        name = name,
                        birth_date = birthDate,
                        gender = "unknown",
                        phone = phone
                    )
                    viewModel.signup(req)
                },
                enabled = !ui.loading,
                colors = ButtonDefaults.buttonColors(containerColor = AuthSecondrayButton),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
            ) {
                Text(
                    text = if (ui.loading) "가입 중..." else "회원가입",
                    color = AuthOnSecondray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onWriteLater() }.padding(vertical = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .clickable { onBackToLogin() }
                        .height(32.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("로그인", color = Color(0xff6ac0e0), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
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

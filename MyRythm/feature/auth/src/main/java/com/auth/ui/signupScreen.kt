package com.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auth.viewmodel.AuthViewModel
import com.domain.model.SignupRequest
import com.shared.ui.components.AuthActionButton
import com.shared.ui.components.AuthGenderDropdown
import com.shared.ui.components.AuthInputField
import com.shared.ui.components.AuthLogoIcon
import com.shared.ui.components.AuthPrimaryButton
import com.shared.ui.components.AuthSecondaryButton
import com.shared.ui.theme.AuthBackground
import com.shared.ui.theme.AuthSecondrayButton
import com.shared.ui.theme.loginTheme

private val SecondaryBtnDisabled = AuthSecondrayButton.copy(alpha = 0.5f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onSignupComplete: () -> Unit = {},
    onBackToLogin: () -> Unit = {},
    socialId: String? = null,
    provider: String? = null
) {
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    var birthYear by rememberSaveable { mutableStateOf("") }
    var birthMonth by rememberSaveable { mutableStateOf("") }
    var birthDay by rememberSaveable { mutableStateOf("") }

    var height by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    var gender by rememberSaveable { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }

    var isPhoneVerificationSent by rememberSaveable { mutableStateOf(false) }
    var isVerificationCompleted by rememberSaveable { mutableStateOf(false) }
    var code by rememberSaveable { mutableStateOf("") }

    val ui by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg ->
            snackbar.showSnackbar(msg)
            if (msg.contains("회원가입 성공")) onSignupComplete()
        }
    }

    fun validNumber(s: String) = s.toDoubleOrNull() != null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AuthBackground,
        snackbarHost = { SnackbarHost(snackbar) },
        // ✅ 내부 스캐폴드 인셋 제거로 상·하 여백 제거
        contentWindowInsets = WindowInsets(0)
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(AuthBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 로고
            AuthLogoIcon()
            Spacer(Modifier.height(24.dp))

            //이메일
            AuthInputField(
                value = email,
                onValueChange = { email = it },
                hint = "이메일",
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            )

            Spacer(Modifier.height(16.dp))

            AuthInputField(
                value = username,
                onValueChange = { username = it },
                hint = "사용자 이름",
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next
            )

            AuthInputField(
                value = password,
                onValueChange = { password = it },
                hint = "비밀번호",
                isPassword = true,
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Password
            )

            Spacer(Modifier.height(24.dp))

            Text("생년월일",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )


            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                AuthInputField(
                    value = birthYear,
                    onValueChange = { birthYear = it.filter { c -> c.isDigit() }.take(4) },
                    hint = "YYYY",
                    modifier = Modifier.weight(1.5f),
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                )

                AuthInputField(
                    value = birthMonth,
                    onValueChange = { birthMonth = it.filter { c -> c.isDigit() }.take(2) },
                    hint = "MM",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                )

                AuthInputField(
                    value = birthDay,
                    onValueChange = { birthDay = it.filter { c -> c.isDigit() }.take(2) },
                    hint = "DD",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(Modifier.height(20.dp))

            // 1114 12:28 성별 드롭다운 컴포넌트화 완료
            // AuthInputField.kt의 AuthGenderDropdown()
            AuthGenderDropdown(
                value = gender,
                onValueChange = { gender = it },
                hint = "성별",
                modifier = Modifier.fillMaxWidth()
            )

            /*
            // 성별 드롭다운_컴포넌트화 완료 - AuthInputField.kt의 AuthGenderDropdown() 사용하기
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded },
            ) {
                OutlinedTextField(
                    value = when (gender) {
                        "M" -> "남성"
                        "F" -> "여성"
                        else -> ""
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("성별") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("남성") }, onClick = { gender = "M"; genderExpanded = false })
                    DropdownMenuItem(text = { Text("여성") }, onClick = { gender = "F"; genderExpanded = false })
                }
            }*/

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                AuthInputField(
                    value = height,
                    onValueChange = { height = it },
                    hint = "키 (cm)",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                )
                AuthInputField(
                    value = weight,
                    onValueChange = { weight = it },
                    hint = "몸무게 (kg)",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                )
            }
            Spacer(Modifier.height(24.dp))

            Text("전화번호 인증 *",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AuthInputField(
                    value = phone,
                    onValueChange = { phone = it },
                    hint = "전화번호 (010-1111-1111)",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Phone,
                    enabled = !isVerificationCompleted
                )
                Spacer(Modifier.width(8.dp))
                AuthActionButton(
                    text = if (isPhoneVerificationSent) "전송됨" else "전송",
                    onClick = {
                        if (phone.isBlank()) {
                            viewModel.emitInfo("전화번호를 입력하세요")
                        } else {
                            isPhoneVerificationSent = true
                            isVerificationCompleted = false
                            code = ""
                            viewModel.emitInfo("인증번호가 전송되었습니다. 테스트 코드는 0000 입니다")
                        }
                    },
                    enabled = !isVerificationCompleted,
                    useLoginTheme = false,
                    modifier = Modifier
                        .height(56.dp)
                        .widthIn(min = 90.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AuthInputField(
                    value = code,
                    onValueChange = { code = it },
                    hint = "인증번호",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Number,
                    enabled = isPhoneVerificationSent && !isVerificationCompleted
                )

                Spacer(Modifier.width(8.dp))

                AuthSecondaryButton(
                    text = "인증",
                    onClick = {
                        if (!isPhoneVerificationSent) {
                            viewModel.emitInfo("먼저 인증번호를 전송하세요")
                            return@AuthSecondaryButton
                        }
                        if (code == "0000") {
                            isVerificationCompleted = true
                            viewModel.emitInfo("전화번호 인증이 완료되었습니다")
                        } else {
                            isVerificationCompleted = false
                            viewModel.emitInfo("인증번호가 올바르지 않습니다. 테스트 코드는 0000 입니다")
                        }
                    },
                    enabled = isPhoneVerificationSent && !isVerificationCompleted,
                    modifier = Modifier
                        .height(56.dp)
                        .widthIn(min = 90.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // 소셜로그인 관련 추가 없이 기존 코드 로직 그대로 두고 컴포넌트화만 진행
            AuthPrimaryButton(
                text = if (ui.loading) "가입 중..." else "회원가입",
                onClick = {
                    val birthDate = "${birthYear}-${birthMonth.padStart(2, '0')}-${birthDay.padStart(2, '0')}"
                    val heightOk = validNumber(height)
                    val weightOk = validNumber(weight)

                    if (
                        email.isBlank() || username.isBlank() || password.isBlank() ||
                        birthYear.length != 4 || birthMonth.isBlank() || birthDay.isBlank() ||
                        !heightOk || !weightOk || phone.isBlank()
                    ) {
                        viewModel.emitInfo("필수 항목을 정확히 입력하세요")
                        return@AuthPrimaryButton
                    }
                    if (!isVerificationCompleted) {
                        viewModel.emitInfo("전화번호 인증을 완료하세요")
                        return@AuthPrimaryButton
                    }

                    val req = SignupRequest(
                        email = email,
                        username = username,
                        password = password,
                        phone = phone,
                        birthDate = birthDate,
                        gender = gender,
                        height = height.toDouble(),
                        weight = weight.toDouble()
                    )
                    viewModel.signup(req)
                },
                enabled = !ui.loading,
                useLoginTheme = false,
                useClickEffect = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
            )
            Spacer(Modifier.height(24.dp))

            AuthSecondaryButton(
                text = "돌아가기",
                onClick = { onBackToLogin() },
                enabled = true,
                useLoginTheme = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )

            Spacer(Modifier.height(16.dp))

            //1114 로그인스크린처럼 텍스트링크버튼으로 단순화
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "이미 계정이 있으신가요?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.loginTheme.loginTertiary,
                    modifier = Modifier
                        .clickable { onBackToLogin() }
                        .padding(vertical = 4.dp)
                )
            }

            /* 1114 이전 부분 코드 블록. 필요 시 살리기
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("이미 계정이 있으신가요?", color = Color.Black, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
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
            }*/
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignupPreview() {
    MaterialTheme {
        SignupScreen()
    }
}

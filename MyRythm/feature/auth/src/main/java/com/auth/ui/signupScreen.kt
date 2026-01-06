package com.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auth.ui.components.EmailVerificationSection
import com.domain.model.SignupRequest
import com.shared.R
import com.shared.ui.components.AuthGenderDropdown
import com.shared.ui.components.AuthInputField
import com.shared.ui.components.AuthLogoIcon
import com.shared.ui.components.AuthPrimaryButton
import com.shared.ui.components.AuthSecondaryButton
import com.shared.ui.components.AuthSectionTitle
import com.shared.ui.components.AuthTextButton
import com.shared.ui.theme.AuthBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onSignupComplete: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
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

    var isEmailCodeSent by rememberSaveable { mutableStateOf(false) }
    var isVerificationCompleted by rememberSaveable { mutableStateOf(false) }
    var code by rememberSaveable { mutableStateOf("") }

    // 전송 횟수 및 타이머 관리
    var sendCount by rememberSaveable { mutableStateOf(0) }
    var remainingSeconds by rememberSaveable { mutableStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }

    val ui by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    val nameText = stringResource(R.string.name)
    val passwordText = stringResource(R.string.auth_password)
    val birthText = stringResource(R.string.birth)
    val yearText = stringResource(R.string.year)
    val monthText = stringResource(R.string.month)
    val dayText = stringResource(R.string.day)
    val heightText = stringResource(R.string.height)
    val weightText = stringResource(R.string.weight)
    val phoneNumberPlaceholderText = stringResource(R.string.phone_number_placeholder)
    val signupLoading = stringResource(R.string.auth_signup_loading)
    val signupText = stringResource(R.string.auth_signup)
    val backText = stringResource(R.string.back)
    val backToLoginMessage = stringResource(R.string.auth_message_backtologin)

    // 타이머 LaunchedEffect
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning && remainingSeconds > 0) {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
            }
            isTimerRunning = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg ->
            when {
                msg.contains("회원가입 성공") -> {
                    snackbar.showSnackbar(msg)
                    onSignupComplete()
                }
                msg.contains("인증 성공") || msg.contains("인증 완료") -> {
                    // 인증 성공은 UI에 이미 표시되므로 스낵바 표시 안 함
                    isVerificationCompleted = true
                    isTimerRunning = false
                }
                msg.contains("인증코드 전송") -> {
                    // 전송 완료도 UI에 표시되므로 스낵바 표시 안 함
                    isEmailCodeSent = true
                    sendCount++
                    remainingSeconds = 180
                    isTimerRunning = true
                }
                else -> snackbar.showSnackbar(msg)
            }
        }
    }

    fun validNumber(s: String) = s.toDoubleOrNull() != null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier.padding(bottom = 40.dp)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(AuthBackground)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            AuthLogoIcon()

            Spacer(Modifier.height(24.dp))

            // 이메일인증+타이머까지 컴포넌트로 넘김_1216 EmailVerificationSection.kt
            EmailVerificationSection(
                email = email,
                code = code,
                isVerificationCompleted = isVerificationCompleted,
                isEmailCodeSent = isEmailCodeSent,
                isTimerRunning = isTimerRunning,
                remainingSeconds = remainingSeconds,
                canSend = email.isNotBlank() && sendCount < 5 && !isVerificationCompleted,
                onEmailChange = {
                    email = it
                    if (isVerificationCompleted) {
                        isVerificationCompleted = false
                        isEmailCodeSent = false
                        sendCount = 0
                        remainingSeconds = 0
                        isTimerRunning = false
                    }
                },
                onSendClick = {
                    if (email.isNotBlank()) {
                        // =============================
                        // TEST용 UI 가상 인증 코드
                        // =============================
                        if (email == "test@test.com") {
                            isEmailCodeSent = true
                            sendCount++
                            remainingSeconds = 180
                            isTimerRunning = true
                            kotlinx.coroutines.MainScope().launch {
                                snackbar.showSnackbar("인증코드가 전송되었습니다.")
                            }
                            return@EmailVerificationSection
                        }
                        // =============================
                        if (sendCount >= 5) {
                            kotlinx.coroutines.MainScope().launch {
                                snackbar.showSnackbar("인증 요청 횟수가 초과되었습니다. 1시간 후 다시 시도해주세요.")
                            }
                        } else {
                            viewModel.updateSignupEmail(email)
                            viewModel.sendCode()
                        }
                    }
                },
                onCodeChange = {
                    code = it
                    viewModel.updateCode(it)
                },
                onVerifyClick = {
                    // =============================
                    // TEST용 UI 가상 인증 성공 분기
                    // =============================
                    if (email == "test@test.com" && code == "1234") {
                        isVerificationCompleted = true
                        isTimerRunning = false
                        kotlinx.coroutines.MainScope().launch  {
                            snackbar.showSnackbar("인증 완료")
                        }
                        return@EmailVerificationSection
                    }
                    // =============================
                    viewModel.updateSignupEmail(email)
                    viewModel.updateCode(code)
                    viewModel.verifyCode()
                }
            )

            Spacer(Modifier.height(12.dp))

            AuthSectionTitle(nameText)
            AuthInputField(
                value = username,
                onValueChange = { username = it },
                hint = nameText,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            AuthSectionTitle(passwordText)
            AuthInputField(
                value = password,
                onValueChange = { password = it },
                hint = passwordText,
                isPassword = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Password
            )

            Spacer(Modifier.height(12.dp))

            AuthSectionTitle(birthText)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AuthInputField(
                    value = birthYear,
                    onValueChange = { birthYear = it.filter { c -> c.isDigit() }.take(4) },
                    hint = yearText,
                    modifier = Modifier.weight(1.5f),
                    keyboardType = KeyboardType.Number
                )
                AuthInputField(
                    value = birthMonth,
                    onValueChange = { birthMonth = it.filter { c -> c.isDigit() }.take(2) },
                    hint = monthText,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
                AuthInputField(
                    value = birthDay,
                    onValueChange = { birthDay = it.filter { c -> c.isDigit() }.take(2) },
                    hint = dayText,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(Modifier.height(20.dp))

            AuthGenderDropdown(
                value = gender,
                onValueChange = { gender = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AuthSectionTitle(heightText)
                }
                Box(modifier = Modifier.weight(1f)) {
                    AuthSectionTitle(weightText)
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AuthInputField(
                    value = height,
                    onValueChange = { height = it },
                    hint = heightText,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
                AuthInputField(
                    value = weight,
                    onValueChange = { weight = it },
                    hint = weightText,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(Modifier.height(12.dp))

            AuthSectionTitle(phoneNumberPlaceholderText)
            AuthInputField(
                value = phone,
                onValueChange = { phone = it },
                hint = phoneNumberPlaceholderText,
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Phone
            )

            Spacer(Modifier.height(24.dp))

            AuthPrimaryButton(
                text = if (ui.loading) signupLoading else signupText,
                onClick = {
                    val birthDate = "${birthYear}-${birthMonth.padStart(2, '0')}-${birthDay.padStart(2, '0')}"
                    val heightOk = validNumber(height)
                    val weightOk = validNumber(weight)

                    if (
                        email.isBlank() || username.isBlank() || password.isBlank() ||
                        birthYear.length != 4 || birthMonth.isBlank() || birthDay.isBlank() ||
                        !heightOk || !weightOk || phone.isBlank()
                    ) {
                        return@AuthPrimaryButton
                    }

                    if (!isVerificationCompleted) {
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            AuthSecondaryButton(
                text = backText,
                onClick = { onBackToLogin() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                AuthTextButton(
                    text = backToLoginMessage,
                    onClick = { onBackToLogin() }
                )
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}
package com.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auth.viewmodel.AuthViewModel
import com.shared.R
import com.shared.ui.components.AuthInputField
import com.shared.ui.components.AuthLogoHeader
import com.shared.ui.components.AuthPrimaryButton
import com.shared.ui.components.AuthSecondaryButton
import com.shared.ui.components.AuthActionButton
import com.shared.ui.theme.AuthBackground
import kotlinx.coroutines.launch


@Composable
fun PwdScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onBackToLogin: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    // 1202 새로운비번설정
    var verified by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 문자열 리소스화 선언
    val emailText = stringResource(R.string.email)
    val newPasswordText = stringResource(R.string.auth_newpassword)
    val sendText = stringResource(R.string.send)
    val sentText = stringResource(R.string.sent)
    val verificationText = stringResource(R.string.verification)
    val verificationCodeText = stringResource(R.string.verification_code)
    val comfirmText = stringResource(R.string.confirm)
    val backtologinText = stringResource(R.string.auth_backtologin)

    // ViewModel 이벤트 감지 (스낵바 표시 및 상태 변경)
    LaunchedEffect(Unit) {
        viewModel.events.collect { msg ->
            snackbar.showSnackbar(msg)
            when (msg) {
                "비밀번호 재설정 인증코드 전송" -> sent = true
                "재설정 인증 성공" -> verified = true
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AuthBackground,
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(50.dp))

            AuthLogoHeader(textLogoResId = R.drawable.auth_myrhythm)

            Spacer(Modifier.height(10.dp))

            // 1202 휴대폰로직->이메일로직 변경 완
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AuthInputField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (verified) verified = false
                        sent = false
                        code = ""
                    },
                    hint = emailText,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Email,
                    enabled = !verified
                )

                Spacer(Modifier.width(8.dp))

                AuthActionButton(
                    text = if (sent) sentText else sendText,
                    onClick = {

                        viewModel.sendResetCode(email) //1202 실제코드

                        /*// 1202 ui테스트용 임시로직
                        sent = true
                        verified = false
                        code = ""
*/
                    },
                    enabled = !sent && email.isNotBlank(),
                    modifier = Modifier
                        .widthIn(min = 90.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 인증번호 입력
            AuthInputField(
                value = code,
                onValueChange = { code = it },
                hint = verificationCodeText,
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Number,
                enabled = !verified
            )

            Spacer(Modifier.width(8.dp))

            AuthActionButton(
                text = verificationText,
                onClick = {
                    viewModel.verifyResetCode(email, code)    //1202 실제코드

                    /*// 1202 테스트용 임시 로직 1234
                    if (code == "1234") {
                        verified = true
                        return@AuthActionButton
                    }*/
                },
                enabled = !verified && code.isNotBlank(),
                modifier = Modifier.widthIn(min = 90.dp)
                )
            }

            if (verified) {
                Spacer(Modifier.height(20.dp))

                AuthInputField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    hint = newPasswordText,
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(58.dp))

            AuthPrimaryButton(
                text = comfirmText,
                onClick = {
                    if (!verified) {
                        scope.launch {snackbar.showSnackbar("이메일 인증을 완료하세요")}
                        return@AuthPrimaryButton
                    }

                    if (newPassword.isBlank()) {
                        scope.launch {snackbar.showSnackbar("새 비밀번호를 입력하세요")}
                        return@AuthPrimaryButton
                    }

                    // 1202 실제코드
                    viewModel.resetPassword(email, newPassword)

                    // 성공하면 로그인 화면으로 이동
                    onBackToLogin()
                },
                enabled = verified,
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

/* 1201 기존 휴대폰인증코드 주석
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
 */
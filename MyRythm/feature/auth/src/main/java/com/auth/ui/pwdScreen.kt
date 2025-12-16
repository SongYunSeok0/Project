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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PwdScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onBackToLogin: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }
    var verified by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val emailText = stringResource(R.string.email)
    val newPasswordText = stringResource(R.string.auth_newpassword)
    val sendText = stringResource(R.string.send)
    val sentText = stringResource(R.string.sent)
    val verificationText = stringResource(R.string.verification)
    val verificationCodeText = stringResource(R.string.verification_code)
    val comfirmText = stringResource(R.string.confirm)
    val backtologinText = stringResource(R.string.auth_backtologin)
    val verifyEmailMessage = stringResource(R.string.auth_message_verify_email)
    val enterNewPasswordMessage = stringResource(R.string.auth_message_enter_new_password)

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
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(AuthBackground)
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            Spacer(Modifier.height(34.dp))

            AuthLogoHeader(textLogoResId = R.drawable.auth_myrhythm)

            Spacer(Modifier.height(10.dp))

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
                        viewModel.sendResetCode(email)
                    },
                    enabled = !sent && email.isNotBlank(),
                    modifier = Modifier.widthIn(min = 90.dp)
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
                    hint = verificationCodeText,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number,
                    enabled = !verified
                )

                Spacer(Modifier.width(8.dp))

                AuthActionButton(
                    text = verificationText,
                    onClick = {
                        viewModel.verifyResetCode(email, code)
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
                        scope.launch { snackbar.showSnackbar(verifyEmailMessage) }
                        return@AuthPrimaryButton
                    }

                    if (newPassword.isBlank()) {
                        scope.launch { snackbar.showSnackbar(enterNewPasswordMessage) }
                        return@AuthPrimaryButton
                    }

                    viewModel.resetPassword(email, newPassword)
                    onBackToLogin()
                },
                enabled = verified,
                modifier = Modifier.fillMaxWidth(),
                useClickEffect = true
            )

            Spacer(Modifier.height(14.dp))

            AuthSecondaryButton(
                text = backtologinText,
                onClick = onBackToLogin,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.weight(1f))

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}
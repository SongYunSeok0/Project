package com.auth.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auth.viewmodel.PasswordResetViewModel
import com.shared.R
import com.shared.ui.components.AuthActionButton
import com.shared.ui.components.AuthInputField
import com.shared.ui.components.AuthLogoHeader
import com.shared.ui.components.AuthPrimaryButton
import com.shared.ui.components.AuthSecondaryButton
import com.shared.ui.theme.authTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PwdScreen(
    modifier: Modifier = Modifier,
    viewModel: PasswordResetViewModel = hiltViewModel(),
    onBackToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val emailText = stringResource(R.string.email)
    val newPasswordText = stringResource(R.string.auth_newpassword)
    val sendText = stringResource(R.string.send)
    val sentText = stringResource(R.string.sent)
    val verificationText = stringResource(R.string.verification)
    val verificationCodeText = stringResource(R.string.verification_code)
    val confirmText = stringResource(R.string.confirm)
    val backtologinText = stringResource(R.string.auth_backtologin)
    val verifyEmailMessage = stringResource(R.string.auth_message_verify_email)
    val enterNewPasswordMessage = stringResource(R.string.auth_message_enter_new_password)

    // 에러 메시지 표시
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // 비밀번호 재설정 성공 시 로그인 화면으로 이동
    LaunchedEffect(uiState.isResetSuccess) {
        if (uiState.isResetSuccess) {
            snackbar.showSnackbar("비밀번호가 재설정되었습니다")
            onBackToLogin()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(MaterialTheme.authTheme.authBackground)
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
                    },
                    hint = emailText,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Email,
                    enabled = !uiState.isCodeVerified
                )

                Spacer(Modifier.width(8.dp))

                AuthActionButton(
                    text = if (uiState.isCodeSent) sentText else sendText,
                    onClick = {
                        viewModel.sendResetCode(email)
                    },
                    enabled = !uiState.isCodeSent && email.isNotBlank() && !uiState.loading,
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
                    enabled = !uiState.isCodeVerified
                )

                Spacer(Modifier.width(8.dp))

                AuthActionButton(
                    text = verificationText,
                    onClick = {
                        viewModel.verifyResetCode(email, code)
                    },
                    enabled = !uiState.isCodeVerified && code.isNotBlank() && !uiState.loading,
                    modifier = Modifier.widthIn(min = 90.dp)
                )
            }

            if (uiState.isCodeVerified) {
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
                text = confirmText,
                onClick = {
                    if (!uiState.isCodeVerified) {
                        scope.launch { snackbar.showSnackbar(verifyEmailMessage) }
                        return@AuthPrimaryButton
                    }

                    if (newPassword.isBlank()) {
                        scope.launch { snackbar.showSnackbar(enterNewPasswordMessage) }
                        return@AuthPrimaryButton
                    }

                    viewModel.resetPassword(email, newPassword)
                },
                enabled = uiState.isCodeVerified && !uiState.loading,
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
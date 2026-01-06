package com.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auth.BuildConfig
import com.auth.ui.components.LocalLoginSection
import com.auth.ui.components.SocialLoginSection
import com.auth.viewmodel.LoginViewModel
import com.auth.viewmodel.SocialLoginViewModel
import com.shared.R
import com.shared.ui.components.AuthLogoHeader
import com.shared.ui.theme.loginTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = hiltViewModel(),
    socialViewModel: SocialLoginViewModel = hiltViewModel(),
    onLogin: (String, String) -> Unit = { _, _ -> },
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {}
) {
    val loginUi by loginViewModel.uiState.collectAsStateWithLifecycle()
    val autoLoginEnabled by loginViewModel.autoLoginEnabled.collectAsStateWithLifecycle()

    val socialUi by socialViewModel.uiState.collectAsStateWithLifecycle()

    // ✅ form은 UI 로컬 상태로 관리 (최소 수정)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    // ✅ 일반 로그인 에러 메시지
    LaunchedEffect(loginUi.errorMessage) {
        loginUi.errorMessage?.let {
            snackbar.showSnackbar(it)
            loginViewModel.clearError()
        }
    }

    // ✅ 소셜 로그인 에러 메시지 (SocialLoginViewModel에 clearError가 있다면 호출)
    LaunchedEffect(socialUi.errorMessage) {
        socialUi.errorMessage?.let {
            snackbar.showSnackbar(it)
            socialViewModel.clearError()
        }
    }

    // ✅ 일반 로그인 성공
    LaunchedEffect(loginUi.isLoggedIn, loginUi.userId) {
        if (loginUi.isLoggedIn) {
            loginUi.userId?.let { uid ->
                onLogin(uid, password)
            }
        }
    }

    // ✅ 소셜 로그인 성공
    LaunchedEffect(socialUi.isLoggedIn, socialUi.userId) {
        if (socialUi.isLoggedIn) {
            socialUi.userId?.let { uid ->
                // 소셜 로그인은 password 의미 없으면 ""로 넘기는 게 더 자연스럽습니다.
                onLogin(uid, "")
            }
        }
    }

    val loading = loginUi.loading || socialUi.loading

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier.padding(bottom = 40.dp)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.loginTheme.loginBackground)
                .padding(padding)
                .imePadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                }

                item {
                    AuthLogoHeader(textLogoResId = R.drawable.login_myrhythm)
                }

                item {
                    LocalLoginSection(
                        email = email,
                        password = password,
                        onEmailChange = { email = it },
                        onPasswordChange = { password = it },
                        autoLoginEnabled = autoLoginEnabled,
                        onAutoLoginToggle = { loginViewModel.setAutoLogin(it) },
                        onForgotPasswordClick = onForgotPassword,
                        loading = loading,
                        onLoginClick = {
                            loginViewModel.login(email, password)
                        },
                        onSignUpClick = onSignUp
                    )
                }

                item {
                    SocialLoginSection(
                        onKakaoClick = {
                            socialViewModel.kakaoOAuth(context = context)
                        },
                        onGoogleClick = {
                            socialViewModel.googleOAuth(
                                context = context,
                                googleClientId = BuildConfig.GOOGLE_CLIENT_ID
                            )
                        }
                    )
                }

                item {
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    }
}

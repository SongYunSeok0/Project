package com.auth.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auth.BuildConfig
import com.auth.ui.components.LocalLoginSection
import com.auth.ui.components.SocialLoginSection
import com.shared.R
import com.shared.ui.components.AuthLogoHeader
import com.shared.ui.theme.loginTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onLogin: (String, String) -> Unit = { _, _ -> },
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {}
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val autoLoginEnabled by viewModel.autoLoginEnabled.collectAsStateWithLifecycle()

    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    Log.e("LoginScreen", "🎨 State 수집: isLoggedIn=${ui.isLoggedIn}, userId=${ui.userId}, loading=${ui.loading}")

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg ->
            Log.e("LoginScreen", "📡 Event 받음: $msg")
            snackbar.showSnackbar(msg)
        }
    }

    LaunchedEffect(ui.isLoggedIn, ui.userId) {
        Log.e("LoginScreen", "🚀 ========== LaunchedEffect 트리거 ==========")
        Log.e("LoginScreen", "🚀 isLoggedIn = ${ui.isLoggedIn}")
        Log.e("LoginScreen", "🚀 userId = ${ui.userId}")
        Log.e("LoginScreen", "🚀 form.email = ${form.email}")
        if (ui.isLoggedIn) {
            val uid = ui.userId
            if (uid != null) {
                Log.e("LoginScreen", "➡ 로그인 성공 → MainRoute 이동 userId=$uid")
                onLogin(uid, form.password)
            } else {
                Log.e("LoginScreen", "❌ 로그인 성공했지만 userId=null → 이동 차단")
            }
        }
    }

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

                item { AuthLogoHeader(textLogoResId = R.drawable.login_myrhythm) }

                item {
                    LocalLoginSection(
                        email = form.email,
                        password = form.password,
                        onEmailChange = { viewModel.updateLoginEmail(it) },
                        onPasswordChange = { viewModel.updateLoginPW(it) },
                        autoLoginEnabled = autoLoginEnabled,
                        onAutoLoginToggle = { viewModel.setAutoLogin(it) },
                        onForgotPasswordClick = onForgotPassword,
                        loading = ui.loading,
                        onLoginClick = { viewModel.login() },
                        onSignUpClick = onSignUp
                    )
                }

                item {
                    SocialLoginSection(
                        onKakaoClick = {
                            Log.e("LoginScreen", "🟡 ========== 카카오 버튼 클릭 ==========")
                            viewModel.kakaoOAuth(
                                context,
                                onResult = { success, message ->
                                    Log.e("LoginScreen", "🟡 카카오 onResult: success=$success, message=$message")
                                },
                                onNeedAdditionalInfo = { _, _ -> }
                            )
                        },
                        onGoogleClick = {
                            Log.e("LoginScreen", "🔵 ========== 구글 버튼 클릭 ==========")
                            viewModel.googleOAuth(
                                context,
                                googleClientId = BuildConfig.GOOGLE_CLIENT_ID,
                                onResult = { success, message ->
                                    Log.e("LoginScreen", "🔵 구글 onResult: success=$success, message=$message")
                                },
                                onNeedAdditionalInfo = { _, _ -> }
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
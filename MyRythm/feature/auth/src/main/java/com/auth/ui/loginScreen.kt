package com.auth.ui

import android.util.Log
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

    // ✅ autoLogin은 UI 로컬 상태로 관리
    var autoLoginEnabled by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg ->
            snackbar.showSnackbar(msg)
        }
    }

    LaunchedEffect(ui.isLoggedIn, ui.userId) {
        if (ui.isLoggedIn) {
            val uid = ui.userId
            if (uid != null) {
                onLogin(uid, form.password)
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

                item {
                    AuthLogoHeader(textLogoResId = R.drawable.login_myrhythm)
                }

                item {
                    LocalLoginSection(
                        email = form.email,
                        password = form.password,
                        onEmailChange = { viewModel.updateLoginEmail(it) },
                        onPasswordChange = { viewModel.updateLoginPW(it) },
                        autoLoginEnabled = autoLoginEnabled,
                        onAutoLoginToggle = { autoLoginEnabled = it },
                        onForgotPasswordClick = onForgotPassword,
                        loading = ui.loading,
                        onLoginClick = {
                            viewModel.login(autoLoginEnabled)
                        },
                        onSignUpClick = onSignUp
                    )
                }

                item {
                    SocialLoginSection(
                        onKakaoClick = {
                            viewModel.kakaoOAuth(
                                context,
                                onResult = { _, _ -> },
                                onNeedAdditionalInfo = { _, _ -> }
                            )
                        },
                        onGoogleClick = {
                            viewModel.googleOAuth(
                                context,
                                googleClientId = BuildConfig.GOOGLE_CLIENT_ID,
                                onResult = { _, _ -> },
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

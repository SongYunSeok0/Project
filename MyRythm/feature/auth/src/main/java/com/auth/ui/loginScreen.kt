package com.auth.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auth.BuildConfig
import com.auth.viewmodel.AuthViewModel
import com.shared.R
import com.shared.ui.components.AuthInputField
import com.shared.ui.components.AuthLogoHeader
import com.shared.ui.components.AuthPrimaryButton
import com.shared.ui.components.AuthSecondaryButton
import com.shared.ui.components.AuthTextButton
import com.shared.ui.theme.AppFieldHeight
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
    val idText = stringResource(R.string.auth_id)
    val passwordText = stringResource(R.string.auth_password)
    val pwMissingMessage = stringResource(R.string.auth_message_password_missing)
    val loginText = stringResource(R.string.auth_login)
    val setAutologinText = stringResource(R.string.auth_setautologin)
    val loginLoading = stringResource(R.string.auth_login_loading)
    val signupText = stringResource(R.string.auth_signup)
    val oauthText = stringResource(R.string.auth_oauth)
    val kakaoLoginText = stringResource(R.string.auth_kakaologin_description)
    val googleLoginText = stringResource(R.string.auth_googlelogin_description)

    val form by viewModel.form.collectAsStateWithLifecycle()
    val ui by viewModel.state.collectAsStateWithLifecycle()

    Log.e("LoginScreen", "🎨 State 수집: isLoggedIn=${ui.isLoggedIn}, userId=${ui.userId}, loading=${ui.loading}")

    val autoLoginEnabled by viewModel.autoLoginEnabled.collectAsStateWithLifecycle()

    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Log.e("LoginScreen", "📡 Event 수집 시작")
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
        snackbarHost = { SnackbarHost(snackbar) },
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
                    Spacer(Modifier.height(10.dp))

                    AuthInputField(
                        value = form.email,
                        onValueChange = { viewModel.updateLoginEmail(it) },
                        hint = idText,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(12.dp))

                    AuthInputField(
                        value = form.password,
                        onValueChange = { viewModel.updateLoginPW(it) },
                        hint = passwordText,
                        isPassword = true,
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Done
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = setAutologinText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.loginTheme.loginTertiary,
                        )
                        Spacer(Modifier.width(4.dp))
                        Switch(
                            checked = autoLoginEnabled,
                            onCheckedChange = { viewModel.setAutoLogin(it) },
                            colors = SwitchDefaults.colors(
                                uncheckedThumbColor = MaterialTheme.loginTheme.loginTertiary,
                                uncheckedTrackColor = MaterialTheme.loginTheme.loginTertiary.copy(alpha = 0.5f),
                                uncheckedBorderColor = MaterialTheme.loginTheme.loginTertiary,
                                checkedThumbColor = MaterialTheme.loginTheme.loginAppName,
                                checkedTrackColor = MaterialTheme.loginTheme.loginAppName.copy(alpha = 0.7f),
                                checkedBorderColor = MaterialTheme.loginTheme.loginTertiary
                            )
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AuthTextButton(
                            text = pwMissingMessage,
                            onClick = { onForgotPassword() }
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    AuthPrimaryButton(
                        text = if (ui.loading) loginLoading else loginText,
                        onClick = { viewModel.login() },
                        modifier = Modifier
                            .fillMaxWidth(),
                        enabled = !ui.loading,
                    )

                    Spacer(Modifier.height(8.dp))

                    AuthSecondaryButton(
                        text = signupText,
                        onClick = onSignUp,
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    Spacer(Modifier.height(30.dp))
                }

                item {
                    var expandedSns by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedSns = !expandedSns }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = oauthText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.loginTheme.loginTertiary
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    if (expandedSns) {
                        Spacer(Modifier.height(14.dp))

                        Image(
                            painter = painterResource(R.drawable.kakao_login_button),
                            contentDescription = kakaoLoginText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(AppFieldHeight)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    Log.e("LoginScreen", "🟡 ========== 카카오 버튼 클릭 ==========")
                                    viewModel.kakaoOAuth(
                                        context,
                                        onResult = { success, message ->
                                            Log.e("LoginScreen", "🟡 카카오 onResult: success=$success, message=$message")
                                        },
                                        onNeedAdditionalInfo = { _, _ -> }
                                    )
                                },
                            contentScale = ContentScale.FillBounds
                        )

                        Spacer(Modifier.height(8.dp))

                        Image(
                            painter = painterResource(R.drawable.google_login_button),
                            contentDescription = googleLoginText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(AppFieldHeight)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    Log.e("LoginScreen", "🔵 ========== 구글 버튼 클릭 ==========")
                                    viewModel.googleOAuth(
                                        context,
                                        googleClientId = BuildConfig.GOOGLE_CLIENT_ID,
                                        onResult = { success, message ->
                                            Log.e("LoginScreen", "🔵 구글 onResult: success=$success, message=$message")
                                        },
                                        onNeedAdditionalInfo = { _, _ -> }
                                    )
                                },
                            contentScale = ContentScale.FillBounds
                        )

                        // 1128 ui보는용 임시로그인
                        Button(
                            onClick = {
                                Log.e("LoginScreen", "🔧 ========== 임시 로그인 버튼 클릭 ==========")
                                onLogin("test_user_123", "test_password")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "🔧 임시 로그인 (테스트용)",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Spacer(Modifier.height(30.dp))
                    }
                }

                item {
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    }
}

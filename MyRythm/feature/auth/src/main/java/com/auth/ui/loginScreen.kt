package com.auth.ui

import com.auth.BuildConfig
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auth.viewmodel.AuthViewModel
import com.shared.R
import com.shared.ui.components.AuthInputField
import com.shared.ui.components.AuthLogoHeader
import com.shared.ui.components.AuthPrimaryButton
import com.shared.ui.components.AuthSecondaryButton
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
    val testLogin = stringResource(R.string.auth_testlogin)
    val oauthText = stringResource(R.string.auth_oauth)
    val kakaoLoginText = stringResource(R.string.auth_kakaologin_description)
    val googleLoginText = stringResource(R.string.auth_googlelogin_description)

    val form by viewModel.form.collectAsStateWithLifecycle()
    val ui by viewModel.state.collectAsStateWithLifecycle()

    Log.e("LoginScreen", "🎨 State 수집: isLoggedIn=${ui.isLoggedIn}, userId=${ui.userId}, loading=${ui.loading}")

    // 1125 자동로그인 진행중
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
            val userId = ui.userId ?: form.email
            Log.e("LoginScreen", "✅ 네비게이션 실행: userId=$userId, password=${form.password}")
            onLogin(userId, form.password)
            Log.e("LoginScreen", "✅ onLogin 호출 완료")
        } else {
            Log.e("LoginScreen", "⏸️ 네비게이션 대기 중")
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.loginTheme.loginBackground)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item { Spacer(Modifier.height(50.dp)) }

                item { AuthLogoHeader(textLogoResId = R.drawable.login_myrhythm) }

                item {
                    Spacer(Modifier.height(10.dp))

                    AuthInputField(
                        value = form.email,
                        onValueChange = { viewModel.updateLoginEmail(it) },
                        hint = idText,
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Next
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

                    // 1125 자동 로그인 진행중 - 토글 디자인만 추가
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
                        Text(
                            text = pwMissingMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.loginTheme.loginTertiary,
                            modifier = Modifier
                                .clickable { onForgotPassword() }
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    AuthPrimaryButton(
                        text = if (ui.loading) loginLoading else loginText,
                        onClick = { viewModel.login() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !ui.loading,
                        useLoginTheme = true,
                        useClickEffect = true
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            onLogin(form.email, form.password)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            testLogin,
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    AuthSecondaryButton(
                        text = signupText,
                        onClick = onSignUp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        useLoginTheme = true
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
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
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

                        Spacer(Modifier.height(14.dp))

                        Image(
                            painter = painterResource(R.drawable.google_login_button),
                            contentDescription = googleLoginText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
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

                        Spacer(Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
private fun PreviewLogin() {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Primary),
        typography = MaterialTheme.typography.copy(
            labelLarge = TextStyle(
                fontFamily = defaultFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = defaultFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            ),
            bodySmall = TextStyle(
                fontFamily = defaultFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp
            )
        )
    ) {
        LoginScreen()
    }
}*/

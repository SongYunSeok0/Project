package com.auth.ui

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
import com.shared.ui.theme.Primary
import com.shared.ui.theme.defaultFontFamily
import com.shared.ui.theme.loginTheme
import kotlinx.coroutines.launch

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
    val loginLoading = stringResource(R.string.auth_login_loading)
    val signupText = stringResource(R.string.auth_signup)
    val testLogin = stringResource(R.string.auth_testlogin)
    val oauthText = stringResource(R.string.auth_oauth)
    val kakaoLoginText = stringResource(R.string.auth_kakaologin_description)
    val googleLoginText = stringResource(R.string.auth_googlelogin_description)

    val form by viewModel.form.collectAsStateWithLifecycle()
    val ui by viewModel.state.collectAsStateWithLifecycle()

    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 이벤트 메시지 수신 → Snackbar 표시
    LaunchedEffect(Unit) {
        viewModel.events.collect { msg ->
            snackbar.showSnackbar(msg)
        }
    }

    // 로그인 성공 시 네비게이션
    LaunchedEffect(ui.isLoggedIn, ui.userId) {
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

                    // 로그인 버튼
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

                    // 테스트 로그인 버튼
                    Button(
                        onClick = {
                            val uid = ui.userId
                            if (uid != null) {
                                onLogin(uid, form.password)
                            } else {
                                scope.launch {
                                    snackbar.showSnackbar("로그인 후 이용해주세요")
                                }
                            }
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

                    // 회원가입 버튼
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

                // SNS 로그인
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
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.FillBounds
                        )

                        Spacer(Modifier.height(14.dp))

                        Image(
                            painter = painterResource(R.drawable.google_login_button),
                            contentDescription = googleLoginText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.FillBounds
                        )

                        Spacer(Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLogin() {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Primary),
        typography = MaterialTheme.typography.copy(
            labelLarge = TextStyle(
                fontFamily = defaultFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp
            )
        )
    ) {
        LoginScreen()
    }
}

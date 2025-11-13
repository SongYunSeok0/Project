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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auth.BuildConfig
import com.auth.viewmodel.AuthViewModel
import com.common.design.R
import com.ui.components.AuthInputField
import com.ui.components.AuthLogoHeader
import com.ui.components.AuthPrimaryButton
import com.ui.components.AuthSecondaryButton
import com.ui.theme.Primary
import com.ui.theme.defaultFontFamily
import com.ui.theme.loginTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onLogin: (String, String) -> Unit = { _, _ -> },
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {},
    onSocialSignUp: (String, String) -> Unit = { _, _ -> }
) {
    var id by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val ui by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    // ViewModel에서 오는 메시지(Snackbar)
    LaunchedEffect(Unit) {
        viewModel.events.collect { msg -> snackbar.showSnackbar(msg) }
    }

    // 로그인 성공 시 네비게이션
    LaunchedEffect(ui.isLoggedIn) {
        if (ui.isLoggedIn) onLogin(id, password)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
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

                item {
                    AuthLogoHeader(textLogoResId = R.drawable.login_myrhythm)
                }

                item {
                    Spacer(Modifier.height(10.dp))

                    // 아이디 입력
                    AuthInputField(
                        value = id,
                        onValueChange = { id = it },
                        hint = "아이디",
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Next
                    )

                    Spacer(Modifier.height(12.dp))

                    // 비밀번호 입력
                    AuthInputField(
                        value = password,
                        onValueChange = { password = it },
                        hint = "비밀번호",
                        isPassword = true,
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Done
                    )

                    Spacer(Modifier.height(8.dp))

                    // 비밀번호 찾기
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "비밀번호를 잊으셨나요?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.loginTheme.loginTertiary,
                            modifier = Modifier
                                .clickable { onForgotPassword() }
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    // 일반 로그인 버튼
                    AuthPrimaryButton(
                        text = if (ui.loading) "로그인 중..." else "Login",
                        onClick = {
                            if (id.isBlank() || password.isBlank()) {
                                viewModel.emitInfo("ID와 비밀번호를 입력해주세요.")
                                return@AuthPrimaryButton
                            }
                            viewModel.login(id, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !ui.loading,
                        useLoginTheme = true,
                        useClickEffect = true
                    )

                    Spacer(Modifier.height(8.dp))

                    // 임시 테스트 로그인 (SKIP)
                    Button(
                        onClick = {
                            onLogin("test_id", "test_pw")
                            viewModel.emitInfo("테스트 로그인으로 즉시 이동합니다.")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "임시 테스트 로그인 (SKIP)",
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // 회원가입 버튼
                    AuthSecondaryButton(
                        text = "회원가입",
                        onClick = onSignUp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        useLoginTheme = true
                    )

                    Spacer(Modifier.height(30.dp))
                }

                // SNS 로그인 영역
                item {
                    var expandedSns by remember { mutableStateOf(false) }

                    // SNS 토글 헤더
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedSns = !expandedSns }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SNS 연동 로그인 하기",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.loginTheme.loginTertiary
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    if (expandedSns) {
                        Spacer(Modifier.height(14.dp))

                        // 카카오 로그인 버튼
                        Image(
                            painter = painterResource(R.drawable.kakao_login_button),
                            contentDescription = "카카오톡 로그인",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.kakaoOAuth(
                                        context,
                                        onResult = { success, message ->
                                            if (success) {
                                                // 소셜 로그인 성공 → 메인으로 이동
                                                onLogin("", "")
                                            } else {
                                                viewModel.emitInfo(message)
                                                Log.e("LoginScreen", "카카오 로그인 실패: $message")
                                            }
                                        },
                                        onNeedAdditionalInfo = { socialId, provider ->
                                            onSocialSignUp(socialId, provider)
                                            Log.d(
                                                "LoginScreen",
                                                "카카오 신규 회원: socialId=$socialId, provider=$provider"
                                            )
                                        }
                                    )
                                },
                            contentScale = ContentScale.FillBounds
                        )

                        Spacer(Modifier.height(14.dp))

                        // 구글 로그인 버튼
                        Image(
                            painter = painterResource(R.drawable.google_login_button),
                            contentDescription = "구글 로그인",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    Log.d("LoginScreen", "GOOGLE_CLIENT_ID = ${BuildConfig.GOOGLE_CLIENT_ID}")

                                    viewModel.googleOAuth(
                                        context,
                                        googleClientId = BuildConfig.GOOGLE_CLIENT_ID,
                                        onResult = { success, message ->
                                            if (success) onLogin("", "") else {
                                                viewModel.emitInfo(message)
                                                Log.e("LoginScreen", "구글 로그인 실패: $message")
                                            }
                                        },
                                        onNeedAdditionalInfo = { socialId, provider ->
                                            onSocialSignUp(socialId, provider)
                                            Log.d("LoginScreen", "구글 신규 회원: $socialId / $provider")
                                        }
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
}

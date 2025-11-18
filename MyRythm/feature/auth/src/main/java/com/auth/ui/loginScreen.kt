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
import androidx.compose.runtime.saveable.rememberSaveable
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
    // 문자열 리소스화 선언
    val idText = stringResource(R.string.auth_id)
    val passwordText = stringResource(R.string.auth_password)
    val pwMissingMessage = stringResource(R.string.auth_message_password_missing)
    val loginText = stringResource(R.string.auth_login)
    val loginLoading = stringResource(R.string.auth_login_loading)
    val signupText = stringResource(R.string.auth_signup)
    val errorIdpwBlank = stringResource(R.string.auth_error_id_pw_blank)
    val testLoginMessage = stringResource(R.string.auth_message_testlogin)
    val testLogin = stringResource(R.string.auth_testlogin)
    val oauthText = stringResource(R.string.auth_oauth)
    val kakaoLoginText = stringResource(R.string.auth_kakaologin_description)
    val googleLoginText = stringResource(R.string.auth_googlelogin_description)

    var id by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val ui = viewModel.state.collectAsState().value
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg -> snackbar.showSnackbar(msg) }
    }
    LaunchedEffect(ui.isLoggedIn) {
        if (ui.isLoggedIn) onLogin(id, password)
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
                        value = id,
                        onValueChange = { id = it },
                        hint = idText,
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Next
                    )

                    Spacer(Modifier.height(12.dp))

                    AuthInputField(
                        value = password,
                        onValueChange = { password = it },
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


                    //// AuthButton.kt 컴포넌트 불러오기 : 클릭 효과(useClickEffect) 포함, 로그인 테마 적용
                    //                AuthPrimaryButton( 부분 병합미완

                    /*병합 전 LoginViewModel과 연결되어있던 원래 코드
                    AuthPrimaryButton(
                        text = "로그인",
                        onClick = {
                            viewModel.login(id, password) { success, message ->
                                if (success) {
                                    onLogin(id, password)
                                } else {
                                    Log.e("LoginScreen", "로그인 실패: $message")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        useLoginTheme = true,
                        useClickEffect = true
                    )*/

                    //1112 수정버전
                    AuthPrimaryButton(
                        text = if (ui.loading) loginLoading else loginText,
                        onClick = {
                            // 유효성 검사는 컴포넌트 내부가 아닌 화면 로직에서 처리
                            if (id.isBlank() || password.isBlank()) {
                                viewModel.emitInfo(errorIdpwBlank) // 사용자 안내 메시지 리소스화 적용
                                return@AuthPrimaryButton
                            }
                            // 뷰모델의 콜백 없는 login 함수 호출
                            // 결과 처리는 뷰모델의 _events와 _state를 Composable에서 관찰하여 처리
                            viewModel.login(id, password)
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),

                        // 뷰모델의 loading 상태에 따라 버튼 활성화/비활성화 결정
                        enabled = !ui.loading,
                        useLoginTheme = true,
                        useClickEffect = true
                    )
                    /* 1112 블록
                    Button(
                        onClick = {
                            if (id.isBlank() || password.isBlank()) {
                                onLogin(id, password)   // ✅ 입력 없이 진행
                                viewModel.emitInfo("입력 없이 진행했습니다")
                                return@Button
                            }
                            viewModel.login(id, password)
                        },
                        enabled = !ui.loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xff6ac0e0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            if (ui.loading) "로그인 중..." else "Login",
                            color = Color.White,
                            fontSize = 24.sp
                        )
                    }*/

                    // 임시 로그인 버튼 추가
                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            // 디버그 및 테스트 용도: 유효성 검사 없이 즉시 메인 화면으로 이동
                            onLogin("test_id", "test_pw")
                            viewModel.emitInfo(testLoginMessage)
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

                    // SNS 토글 헤더 (글자만 표시, 클릭 영역은 Row 전체)
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
                        Spacer(Modifier.width(8.dp)) // 가운데 정렬을 위한 여백은 유지
                    }

                    // 조건부 렌더링: 확장되었을 때만 소셜 로그인 이미지 버튼 표시
                    if (expandedSns) {
                        Spacer(Modifier.height(14.dp)) // 헤더와 버튼 사이 여백

                        // 카카오 로그인 버튼 (PNG 이미지)
                        Image(
                            painter = painterResource(R.drawable.kakao_login_button), // 이미지 버튼 리소스 ID 가정
                            contentDescription = kakaoLoginText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.kakaoOAuth(
                                        context,
                                        onResult = { success, message ->
                                            if (success) {
                                                onLogin("", "")
                                            } else {
                                                Log.e("LoginScreen", "카카오 로그인 실패: $message")
                                            }
                                        },
                                        onNeedAdditionalInfo = { socialId, provider ->
                                            onSocialSignUp(socialId, provider)
                                            Log.d("LoginScreen", "카카오 신규 회원: socialId=$socialId, provider=$provider")
                                        }
                                    )
                                },
                            contentScale = ContentScale.FillBounds
                        )

                        Spacer(Modifier.height(14.dp))

                        // 구글 로그인 버튼 (PNG 이미지)
                        Image(
                            painter = painterResource(R.drawable.google_login_button),
                            contentDescription = googleLoginText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.googleOAuth(
                                        context,
                                        googleClientId = BuildConfig.GOOGLE_CLIENT_ID,
                                        onResult = { success, message ->
                                            if (success) {
                                                onLogin("", "")
                                            } else {
                                                Log.e("LoginScreen", "구글 로그인 실패: $message")
                                            }
                                        },
                                        onNeedAdditionalInfo = { socialId, provider ->
                                            onSocialSignUp(socialId, provider)
                                            Log.d(
                                                "LoginScreen", "구글 신규 회원: socialId=$socialId, provider=$provider"
                                            )
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
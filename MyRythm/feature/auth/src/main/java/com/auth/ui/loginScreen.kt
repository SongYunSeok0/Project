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
import com.auth.viewmodel.AuthViewModel
import com.common.design.R
import com.ui.components.AuthInputField
import com.ui.components.AuthLogoHeader
import com.ui.components.AuthPrimaryButton
import com.ui.components.AuthSecondaryButton
import com.ui.theme.Primary
import com.ui.theme.defaultFontFamily
import com.ui.theme.loginTheme

//val BalooThambi = FontFamily(Font(R.font.baloo_thambi, FontWeight.Bold))

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
                        hint = "아이디",
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Next
                    )

                    Spacer(Modifier.height(12.dp))

                    // AuthInputField.kt 컴포넌트 불러오기 : 비밀번호 토글 버튼 로직은 AuthInputField.kt 컴포넌트 내에 존재함
                    AuthInputField(
                        value = password,
                        onValueChange = { password = it },
                        hint = "비밀번호",
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
                            text = "비밀번호를 잊으셨나요?",
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
                        // 💡 텍스트 설정: 로딩 상태에 따라 버튼 텍스트가 바뀌도록 설정해야 합니다.
                        text = if (ui.loading) "로그인 중..." else "Login",
                        onClick = {
                            // [유효성 검사]: 컴포넌트 내부가 아닌 여기(화면 로직)에서 처리하는 것이 좋습니다.
                            if (id.isBlank() || password.isBlank()) {
                                viewModel.emitInfo("ID와 비밀번호를 입력해주세요.") // 사용자에게 안내
                                return@AuthPrimaryButton
                            }
                            // ✅ 뷰모델의 콜백 없는 login 함수 호출
                            // 결과 처리는 뷰모델의 _events와 _state를 Composable에서 관찰하여 처리됩니다.
                            viewModel.login(id, password)

                            // ⚠️ 주의: 이 방식으로는 onLogin(화면 이동)을 즉시 처리할 수 없으므로,
                            // onLogin 호출은 반드시 Composable이 viewModel.events를 관찰하는 곳에 구현되어야 합니다.
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

                    // 💡 [여기에 임시 로그인 버튼 추가] ---------------------------------------------
                    Spacer(Modifier.height(8.dp)) // 기존 버튼과의 간격

                    Button(
                        onClick = {
                            // ⚠️ 디버그 및 테스트 용도: 유효성 검사 없이 즉시 메인 화면으로 이동
                            onLogin("test_id", "test_pw")
                            viewModel.emitInfo("테스트 로그인으로 즉시 이동합니다.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary), // 눈에 띄게 다른 색상 사용
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp), // 기존 버튼보다 작게
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "임시 테스트 로그인 (SKIP)",
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    //여기부터 다시 병합해둔 부분
                    Spacer(Modifier.height(14.dp))

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
                // 1107 16:48 추가중
                item {
                    var expandedSns by remember { mutableStateOf(false) }
                    // 아이콘 리소스 제거 (R.drawable.up_chevron 등)

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
                            text = "SNS 연동 로그인 하기",
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
                                                // 소셜 로그인 성공 시 onLogin 호출
                                                onLogin("", "")
                                            } else {
                                                Log.e("LoginScreen", "카카오 로그인 실패: $message")
                                            }
                                        },
                                        onNeedAdditionalInfo = { socialId, provider ->
                                            // 추가 정보 필요 시 회원가입 화면으로 이동
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
                            contentDescription = "구글 로그인",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.googleOAuth(
                                        context,
                                        onResult = { success, message ->
                                            if (success) {
                                                // 소셜 로그인 성공 시 onLogin 호출
                                                onLogin("", "")
                                            } else {
                                                Log.e("LoginScreen", "구글 로그인 실패: $message")
                                            }
                                        },
                                        onNeedAdditionalInfo = { socialId, provider ->
                                            // 추가 정보 필요 시 처리
                                            onSocialSignUp(socialId, provider)
                                            Log.d(
                                                "LoginScreen", "구글 신규 회원: socialId=$socialId, provider=$provider"
                                            )
                                        }
                                    )
                                },
                            contentScale = ContentScale.FillBounds
                        )

                        Spacer(Modifier.height(30.dp)) // SNS 버튼 아래 여백
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
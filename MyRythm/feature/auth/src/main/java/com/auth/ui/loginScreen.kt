package com.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auth.viewmodel.AuthViewModel
import com.common.design.R

val BalooThambi = FontFamily(Font(R.font.baloo_thambi, FontWeight.Bold))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onLogin: (String, String) -> Unit = { _, _ -> },
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {}
) {
    var id by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val ui = viewModel.state.collectAsState().value
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg -> snackbar.showSnackbar(msg) }
    }
    LaunchedEffect(ui.isLoggedIn) {
        if (ui.isLoggedIn) onLogin(id, password)
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xff6ae0d9))
                .padding(padding)
                .padding(horizontal = 32.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "앱 로고",
                modifier = Modifier
                    .fillMaxWidth(0.40f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "My Rhythm",
                color = Color(0xFFC9F8F6),
                fontSize = 65.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BalooThambi
            )

            Spacer(Modifier.height(60.dp))

            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("아이디(이메일 또는 사용자명)") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "비밀번호를 잊으셨나요?",
                color = Color(0xff77a3a1),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onForgotPassword() }
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (id.isBlank() || password.isBlank()) {
                        viewModel.emitInfo("아이디와 비밀번호를 입력하세요")
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
            }
            Spacer(Modifier.height(10.dp))
            
            Button(
                onClick = {
                    onLogin(id, password) // ← 바로 이동
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff6ac0e0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("임시 로그인", color = Color.White, fontSize = 24.sp)
            }

            Spacer(Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "계정이 없으신가요?", color = Color.Black, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .clickable { onSignUp() }
                        .height(32.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "회원가입",
                            color = Color(0xff6ac0e0),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

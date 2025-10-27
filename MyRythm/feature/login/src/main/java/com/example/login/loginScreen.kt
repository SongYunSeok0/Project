package com.example.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import com.example.common.design.R

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLogin: (id: String, pw: String) -> Unit = { _, _ -> },
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {}
) {
    var id by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var pwVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xff6ae0d9))
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "logo",
                modifier = Modifier.size(96.dp).clip(CircleShape)
            )
            Spacer(Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.login_myrhythm),
                contentDescription = "title",
                modifier = Modifier.width(220.dp).height(64.dp)
            )

            Spacer(Modifier.height(40.dp))

            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                placeholder = { Text("아이디", color = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,          // 내부 배경 흰색 (포커스 상태)
                    unfocusedContainerColor = Color.White,        // 내부 배경 흰색 (비포커스 상태)
                    focusedIndicatorColor = Color(0xFF6AC0E0),    // 포커스 시 테두리 민트색
                    unfocusedIndicatorColor = Color.LightGray,    // 비포커스 시 테두리 회색
                    cursorColor = Color(0xFF6AC0E0),              // 커서 민트색
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = pw,
                onValueChange = { pw = it },
                placeholder = { Text("비밀번호", color = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (pwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { pwVisible = !pwVisible }) {
                        Icon(
                            imageVector = if (pwVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "toggle password",
                            tint = Color(0xFF6AC0E0) // 민트색 포인트
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,          // 내부 배경 흰색 (포커스 상태)
                    unfocusedContainerColor = Color.White,        // 내부 배경 흰색 (비포커스 상태)
                    focusedIndicatorColor = Color(0xFF6AC0E0),    // 포커스 테두리 민트색
                    unfocusedIndicatorColor = Color.LightGray,    // 비포커스 테두리 회색
                    cursorColor = Color(0xFF6AC0E0),              // 커서 민트색
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "비밀번호를 잊으셨나요?",
                color = Color(0xFF2F6B73),
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onForgotPassword() }
                    .padding(vertical = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onLogin(id, pw) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6AC0E0))
            ) {
                Text("로그인", color = Color.White)
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onSignUp,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, Color.White)
            ) {
                Text("회원가입", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLogin() {
    LoginScreen()
}

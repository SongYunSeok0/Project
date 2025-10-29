package com.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.common.design.R

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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(50.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "logo",
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.height(10.dp))

            Image(
                painter = painterResource(id = R.drawable.login_myrhythm),
                contentDescription = "title",
                modifier = Modifier
                    .width(320.dp)
                    .height(96.dp)
            )

            Spacer(Modifier.height(30.dp))

            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                placeholder = { Text("아이디", color = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color(0xFF6AC0E0),
                    unfocusedIndicatorColor = Color.LightGray,
                    cursorColor = Color(0xFF6AC0E0),
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
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (pwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { pwVisible = !pwVisible }) {
                        Icon(
                            imageVector = if (pwVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "toggle password",
                            tint = Color(0xFF6AC0E0)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color(0xFF6AC0E0),
                    unfocusedIndicatorColor = Color.LightGray,
                    cursorColor = Color(0xFF6AC0E0),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "비밀번호를 잊으셨나요?",
                color = Color(0xFF2F6B73),
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onForgotPassword() }
                    .padding(vertical = 4.dp)
            )

            // 🔹 버튼 전 여백 가변 (아래로 내릴수록 값 줄이기)
            Spacer(Modifier.weight(0.15f))

            Button(
                onClick = { onLogin(id, pw) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6AC0E0))
            ) {
                Text("로그인", color = Color.White)
            }

            Spacer(Modifier.height(14.dp))

            OutlinedButton(
                onClick = onSignUp,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, Color.White)
            ) {
                Text("회원가입", color = Color.White)
            }

            Spacer(Modifier.height(120.dp)) // 하단 살짝 띄움
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLogin() {
    LoginScreen()
}

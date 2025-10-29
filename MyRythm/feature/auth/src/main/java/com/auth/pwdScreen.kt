package com.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.common.design.R

@Composable
fun PwdScreen(
    modifier: Modifier = Modifier,
    onSendCode: (phone: String) -> Unit = {},
    onConfirm: (code: String) -> Unit = {},
    onGoLogin: () -> Unit = {}
) {
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xffb5e5e1))
            .padding(horizontal = 24.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
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

            // 휴대폰 번호 + 전송 버튼
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("휴대폰 번호") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
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
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { onSendCode(phone) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(56.dp)
                ) { Text("전송") }
            }

            Spacer(Modifier.height(12.dp))

            // 인증번호 입력
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                placeholder = { Text("인증번호") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(Modifier.height(16.dp))

            // 확인 버튼
            Button(
                onClick = { onConfirm(code) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6AC0E0))
            ) { Text("확인", color = Color.White) }

            Spacer(Modifier.height(12.dp))

            // 로그인으로
            OutlinedButton(
                onClick = onGoLogin,
                enabled = true, // 항상 활성화
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF6AC0E0)
                )
            ) {
                Text("로그인으로", color = Color(0xFF6AC0E0))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPwd() {
    PwdScreen()
}

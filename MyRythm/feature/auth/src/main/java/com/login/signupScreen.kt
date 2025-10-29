package com.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import com.common.design.R
import com.sesac.design.ui.theme.AuthBackground
import com.sesac.design.ui.theme.AuthOnPrimary
import com.sesac.design.ui.theme.AuthOnSecondray
import com.sesac.design.ui.theme.AuthSecondrayButton

@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    onSendCode: (phone: String) -> Unit = {},
    onVerify: (code: String) -> Unit = {},
    onComplete: () -> Unit = {},          // ← 완료 시 로그인으로 이동 처리
    onWriteLater: () -> Unit = {}
) {
    // 입력 상태
    var name by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    // 인증 상태
    var sent by remember { mutableStateOf(false) }
    var verified by remember { mutableStateOf(false) }

    val tfColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        focusedIndicatorColor = AuthSecondrayButton,
        unfocusedIndicatorColor = Color.LightGray,
        cursorColor = AuthSecondrayButton,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black
    )

    Scaffold(modifier = modifier.fillMaxSize()) { inner ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(AuthBackground)
                .padding(horizontal = 24.dp, vertical = 30.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "logo",
                modifier = Modifier.size(120.dp).clip(CircleShape)
            )
            Spacer(Modifier.height(24.dp))

            // 이름
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                placeholder = { Text("이름", color = AuthOnPrimary.copy(alpha = .6f)) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = tfColors
            )

            Spacer(Modifier.height(24.dp))

            Text("생년월일", color = AuthOnPrimary, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())

            // 생년/월/일
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = year, onValueChange = { year = it },
                    placeholder = { Text("1995") },
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.5f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = tfColors
                )
                OutlinedTextField(
                    value = month, onValueChange = { month = it },
                    placeholder = { Text("1") },
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = tfColors
                )
                OutlinedTextField(
                    value = day, onValueChange = { day = it },
                    placeholder = { Text("1") },
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = tfColors
                )
            }

            Spacer(Modifier.height(24.dp))

            // 키 / 몸무게
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = height, onValueChange = { height = it },
                    placeholder = { Text("키(cm)") },
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = tfColors
                )
                OutlinedTextField(
                    value = weight, onValueChange = { weight = it },
                    placeholder = { Text("몸무게(kg)") },
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = tfColors
                )
            }

            Spacer(Modifier.height(24.dp))

            Text("전화번호 인증 *", color = AuthOnPrimary, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())

            // 전화번호 + 전송
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    placeholder = { Text("010-1111-1111") },
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    colors = tfColors
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        sent = true
                        onSendCode(phone)
                    },
                    enabled = !sent && phone.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AuthSecondrayButton, disabledContainerColor = AuthSecondrayButton.copy(alpha = .5f))
                ) { Text(if (sent) "전송됨" else "전송", color = AuthOnSecondray) }
            }

            Spacer(Modifier.height(12.dp))

            // 인증번호 + 인증
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = code, onValueChange = { code = it },
                    placeholder = { Text("인증번호") },
                    singleLine = true, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = tfColors
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        verified = true
                        onVerify(code)
                    },
                    enabled = sent && code.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AuthSecondrayButton, disabledContainerColor = AuthSecondrayButton.copy(alpha = .5f))
                ) { Text("인증", color = AuthOnSecondray) }
            }

            Spacer(Modifier.height(24.dp))

            // 프로필 설정 완료
            Button(
                onClick = { onComplete() },          // ← 로그인으로 이동 연결
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuthSecondrayButton,
                    disabledContainerColor = AuthSecondrayButton.copy(alpha = .5f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(62.dp)
            ) { Text("회원 가입 완료", color = AuthOnSecondray, fontSize = 14.sp) }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onWriteLater() }.padding(vertical = 8.dp)
            ) {
                Text("나중에 작성하기", color = Color.Black, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text("(일부 기능 제한)", color = Color(0xFF7CC8E4), fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignupScreenPreview() {
    SignupScreen()
}

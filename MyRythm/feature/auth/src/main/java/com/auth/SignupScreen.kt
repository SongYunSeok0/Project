package com.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.common.design.R
import com.auth.data.model.UserSignupRequest
import com.auth.viewmodel.SignupViewModel
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.ui.theme.AuthBackground
import com.ui.theme.AuthOnPrimary
import com.ui.theme.AuthOnSecondray
import com.ui.theme.AuthSecondrayButton

val ButtonDisabled = AuthSecondrayButton.copy(alpha = 0.5f)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    viewModel: SignupViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier,
    onSignupComplete: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    // 입력 상태
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    var isPhoneVerificationSent by remember { mutableStateOf(false) }
    var isVerificationCompleted by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AuthBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 로고
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "MyRhythm Logo Icon",
                modifier = Modifier
                    .fillMaxWidth(0.40f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // My Rhythm 텍스트
            Text(
                text = "My Rhythm",
                color = Color(0xff5db0a8),
                fontSize = 65.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BalooThambi
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 아이디 입력
            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("아이디") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 비밀번호 입력
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 이름 입력
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("이름") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 생년월일
            Text(
                text = "생년월일",
                color = AuthOnPrimary,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = birthYear,
                    onValueChange = { birthYear = it },
                    label = { Text("YYYY") },
                    modifier = Modifier.weight(1.5f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = birthMonth,
                    onValueChange = { birthMonth = it },
                    label = { Text("MM") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = birthDay,
                    onValueChange = { birthDay = it },
                    label = { Text("DD") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 키 / 몸무게
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("키 (cm)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("몸무게 (kg)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 전화번호 인증
            Text(
                text = "전화번호 인증 *",
                color = AuthOnPrimary,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("전화번호") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { isPhoneVerificationSent = true },
                    enabled = !isPhoneVerificationSent,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPhoneVerificationSent) ButtonDisabled else AuthSecondrayButton,
                        contentColor = AuthOnSecondray
                    )
                ) {
                    Text(text = if (isPhoneVerificationSent) "전송됨" else "전송")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("인증번호") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { isVerificationCompleted = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuthSecondrayButton,
                        contentColor = AuthOnSecondray
                    )
                ) {
                    Text("인증")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 회원가입 버튼
            Button(
                onClick = {
                    val birthDate = "${birthYear}-${birthMonth.padStart(2, '0')}-${birthDay.padStart(2, '0')}"
                    val user = UserSignupRequest(
                        id = id,
                        password = password,
                        name = name,
                        birth_date = birthDate,
                        gender = "unknown",
                        phone = phone
                    )
                    viewModel.signup(user) { success, msg ->
                        message = msg
                        if (success) onSignupComplete()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AuthSecondrayButton),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
            ) {
                Text(
                    text = "회원가입",
                    color = AuthOnSecondray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = message, color = Color.DarkGray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 이미 계정이 있으신가요? → 로그인
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "이미 계정이 있으신가요?",
                    color = Color.Black,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .clickable { onBackToLogin() }
                        .height(32.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "로그인",
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

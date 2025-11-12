package com.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auth.viewmodel.AuthViewModel
import com.common.design.R
import com.domain.model.SignupRequest
import com.ui.theme.Colors

private val SecondaryBtnDisabled = Colors.AuthSecondrayButton.copy(alpha = 0.5f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onSignupComplete: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    var birthYear by rememberSaveable { mutableStateOf("") }
    var birthMonth by rememberSaveable { mutableStateOf("") }
    var birthDay by rememberSaveable { mutableStateOf("") }

    var height by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    var gender by rememberSaveable { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }

    var isPhoneVerificationSent by rememberSaveable { mutableStateOf(false) }
    var isVerificationCompleted by rememberSaveable { mutableStateOf(false) }
    var code by rememberSaveable { mutableStateOf("") }

    val ui = viewModel.state.collectAsState().value
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg ->
            snackbar.showSnackbar(msg)
            if (msg.contains("회원가입 성공")) onSignupComplete()
        }
    }

    fun validNumber(s: String) = s.toDoubleOrNull() != null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Colors.AuthBackground,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "MyRhythm Logo Icon",
                modifier = Modifier
                    .fillMaxWidth(0.40f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "My Rhythm",
                color = Color(0xff5db0a8),
                fontSize = 65.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BalooThambi
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("이메일") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("사용자 이름") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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

            Spacer(Modifier.height(20.dp))

            Text(
                text = "생년월일",
                color = Colors.AuthOnPrimary,
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
                    onValueChange = { birthYear = it.filter { c -> c.isDigit() }.take(4) },
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
                    onValueChange = { birthMonth = it.filter { c -> c.isDigit() }.take(2) },
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
                    onValueChange = { birthDay = it.filter { c -> c.isDigit() }.take(2) },
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

            Spacer(Modifier.height(20.dp))

            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded },
            ) {
                OutlinedTextField(
                    value = when (gender) {
                        "male" -> "남성"
                        "female" -> "여성"
                        else -> ""
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("성별") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                    DropdownMenuItem(text = { Text("남성") }, onClick = { gender = "M"; genderExpanded = false })
                    DropdownMenuItem(text = { Text("여성") }, onClick = { gender = "F"; genderExpanded = false })
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = height, onValueChange = { height = it },
                    label = { Text("키 (cm)") }, modifier = Modifier.weight(1f),
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
                    value = weight, onValueChange = { weight = it },
                    label = { Text("몸무게 (kg)") }, modifier = Modifier.weight(1f),
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

            Spacer(Modifier.height(24.dp))

            Text(
                text = "전화번호 인증 *",
                color = Colors.AuthOnPrimary,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("전화번호") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    enabled = !isVerificationCompleted,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (phone.isBlank()) {
                            viewModel.emitInfo("전화번호를 입력하세요")
                        } else {
                            isPhoneVerificationSent = true
                            isVerificationCompleted = false
                            code = ""
                            viewModel.emitInfo("인증번호가 전송되었습니다. 테스트 코드는 0000 입니다")
                        }
                    },
                    enabled = !isVerificationCompleted, // 인증 완료되면 전송 비활성화
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPhoneVerificationSent) SecondaryBtnDisabled else Colors.AuthSecondrayButton,
                        contentColor = Colors.AuthOnSecondray
                    )
                ) { Text(if (isPhoneVerificationSent) "전송됨" else "전송") }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("인증번호") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = isPhoneVerificationSent && !isVerificationCompleted,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (!isPhoneVerificationSent) {
                            viewModel.emitInfo("먼저 인증번호를 전송하세요")
                            return@Button
                        }
                        if (code == "0000") {
                            isVerificationCompleted = true
                            viewModel.emitInfo("전화번호 인증이 완료되었습니다")
                        } else {
                            isVerificationCompleted = false
                            viewModel.emitInfo("인증번호가 올바르지 않습니다. 테스트 코드는 0000 입니다")
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    enabled = isPhoneVerificationSent && !isVerificationCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Colors.AuthSecondrayButton,
                        contentColor = Colors.AuthOnSecondray
                    )
                ) { Text("인증") }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val birthDate = "${birthYear}-${birthMonth.padStart(2, '0')}-${birthDay.padStart(2, '0')}"
                    val heightOk = validNumber(height)
                    val weightOk = validNumber(weight)

                    if (
                        email.isBlank() || username.isBlank() || password.isBlank() ||
                        birthYear.length != 4 || birthMonth.isBlank() || birthDay.isBlank() ||
                        !heightOk || !weightOk || phone.isBlank()
                    ) {
                        viewModel.emitInfo("필수 항목을 정확히 입력하세요")
                        return@Button
                    }
                    if (!isVerificationCompleted) {
                        viewModel.emitInfo("전화번호 인증을 완료하세요")
                        return@Button
                    }

                    val req = SignupRequest(
                        email = email,
                        username = username,
                        password = password,
                        phone = phone,
                        birthDate = birthDate,
                        gender = gender,
                        height = height.toDouble(),
                        weight = weight.toDouble()
                    )
                    viewModel.signup(req)
                },
                enabled = !ui.loading,
                colors = ButtonDefaults.buttonColors(containerColor = Colors.AuthSecondrayButton),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
            ) {
                Text(
                    text = if (ui.loading) "가입 중..." else "회원가입",
                    color = Colors.AuthOnSecondray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onBackToLogin() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffb3e5fc)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "돌아가기",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("이미 계정이 있으신가요?", color = Color.Black, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
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
                        Text("로그인", color = Color(0xff6ac0e0), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

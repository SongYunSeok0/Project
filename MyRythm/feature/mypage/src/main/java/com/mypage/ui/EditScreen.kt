package com.mypage.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shared.R
import com.mypage.viewmodel.EditProfileEvent
import com.mypage.viewmodel.EditProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val context = LocalContext.current

    // ⚡ 서버에서 받은 값으로 초기값 설정
    var name by remember(profile) { mutableStateOf(profile?.username ?: "") }
    var height by remember(profile) { mutableStateOf(profile?.height?.toString() ?: "") }
    var weight by remember(profile) { mutableStateOf(profile?.weight?.toString() ?: "") }
    var age by remember(profile) { mutableStateOf(profile?.age?.toString() ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone?.toString() ?: "") }

    // 보호자 이메일 & 인증 상태 관리
    var prot_email by remember(profile) { mutableStateOf(profile?.prot_email?.toString() ?: "") }
    // 기존에 보호자 이메일이 있다면 이미 인증된 것으로 간주
    var isProtEmailVerified by remember(profile) { mutableStateOf(!profile?.prot_email.isNullOrBlank()) }
    var isProtEmailSent by remember { mutableStateOf(false) }
    var protEmailCode by remember { mutableStateOf("") }

    // 본인 이메일
    var email by remember { mutableStateOf("") }
    LaunchedEffect(profile) {
        profile?.let {
            email = it.email ?: ""
        }
    }

    // 문자열 리소스
    val emailText = stringResource(R.string.email)
    val nameText = stringResource(R.string.name)
    val heightText = stringResource(R.string.height)
    val weightText = stringResource(R.string.weight)
    val ageText = stringResource(R.string.age)
    val phoneNumberPlaceholderText = stringResource(R.string.phone_number_placeholder)
    val editDone = stringResource(R.string.edit_done)
    val sendText = stringResource(R.string.send)
    val sentText = stringResource(R.string.sent)
    val verificationText = stringResource(R.string.verification)

    // 저장 이벤트 처리
    LaunchedEffect(true) {
        viewModel.events.collect { event ->
            when (event) {
                EditProfileEvent.SaveSuccess -> {
                    Toast.makeText(context, "저장되었습니다!", Toast.LENGTH_SHORT).show()
                    onDone()
                }
                EditProfileEvent.SaveFailed -> {
                    Toast.makeText(context, "저장 실패!", Toast.LENGTH_SHORT).show()
                }
                EditProfileEvent.LoadFailed -> {
                    Toast.makeText(context, "프로필 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // (프로필 사진 UI 등 생략된 부분은 여기에 포함됨)

        // 🔹 입력 필드
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ReadonlyField(nameText, name)
            EditableField(heightText, height) { height = it }
            EditableField(weightText, weight) { weight = it }
            ReadonlyField(ageText, age)
            ReadonlyField(emailText, email)
            EditableField(phoneNumberPlaceholderText, phone) { phone = it }
            // ❌ [삭제] 중복된 기존 보호자 이메일 필드 제거
            // EditableField("보호자 이메일 주소", prot_email) { prot_email = it }
        }

        // ⭐ [수정] 보호자 이메일 인증 UI
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "보호자 이메일 주소", fontSize = 14.sp, color = Color(0xff3b566e))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = prot_email,
                    onValueChange = {
                        prot_email = it
                        // 이메일이 변경되면 인증 상태 초기화
                        isProtEmailVerified = false
                        isProtEmailSent = false
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 전송 버튼
                Button(
                    onClick = {
                        if (prot_email.isNotBlank()) {
                            viewModel.sendEmailCode(prot_email)
                            isProtEmailSent = true
                            isProtEmailVerified = false
                            protEmailCode = ""
                            Toast.makeText(context, "인증코드가 전송되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isProtEmailVerified,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(text = if (isProtEmailSent) sentText else sendText, fontSize = 14.sp)
                }
            }
        }

        // ⭐ 인증번호 입력 칸
        if (isProtEmailSent && !isProtEmailVerified) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "인증번호", fontSize = 14.sp, color = Color(0xff3b566e))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = protEmailCode,
                        onValueChange = { protEmailCode = it },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // 확인 버튼
                    Button(
                        onClick = {
                            viewModel.verifyEmailCode(prot_email, protEmailCode) { isSuccess ->
                                if (isSuccess) {
                                    isProtEmailVerified = true
                                    Toast.makeText(context, "인증되었습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "인증번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text(text = verificationText, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 저장 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (prot_email.isBlank() || isProtEmailVerified) MaterialTheme.colorScheme.primary
                    else Color.Gray
                )
                .clickable {
                    if (prot_email.isNotBlank() && !isProtEmailVerified) {
                        Toast.makeText(context, "보호자 이메일 인증을 완료해주세요.", Toast.LENGTH_SHORT).show()
                        return@clickable
                    }

                    viewModel.saveProfile(
                        username = name,
                        heightText = height,
                        weightText = weight,
                        ageText = age,
                        email = email,
                        phone = phone,
                        prot_email = prot_email,
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.save),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = editDone,
                    color = MaterialTheme.colorScheme.surface,
                    fontSize = 16.sp
                )
            }
        }

    } // 닫는 괄호 위치 수정됨 (Main Column 종료)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, fontSize = 14.sp, color = Color(0xff3b566e))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
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
    }
}

@Composable
fun ReadonlyField(label: String, value: String?) {
    OutlinedTextField(
        value = value ?: "",
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth()
    )
}
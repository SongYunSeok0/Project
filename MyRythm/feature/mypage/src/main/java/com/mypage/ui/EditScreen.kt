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
import com.shared.ui.components.AuthGenderDropdown
import com.mypage.viewmodel.MyPageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel(),
    myPageVm: MyPageViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()

    val isLocal = !profile?.email.isNullOrEmpty()

    val initialName = profile?.username
    var name by remember(profile) { mutableStateOf(initialName ?: "") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var protEmail by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    /*
     // 1125 로컬/소셜 구분 (이메일 유무 기준)
    val isLocal = !profile?.email.isNullOrEmpty()

    // --- 서버값 초기화 ---
    // ⚡ 서버에서 받은 값으로 초기값 설정
    var name by remember(profile) { mutableStateOf(profile?.username ?: "") }
    var height by remember(profile) { mutableStateOf(profile?.height?.toString() ?: "") }
    var weight by remember(profile) { mutableStateOf(profile?.weight?.toString() ?: "") }
    var birthDate by rememberSaveable(profile) { mutableStateOf(profile?.birth_date ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
     */

    // 데이터 없으면 1회 입력 있으면 수정 불가
    val hasName = !initialName.isNullOrBlank()
    val hasGender = gender.isNotBlank()

    var isProtEmailVerified by remember { mutableStateOf(false) }
    var isProtEmailSent by remember { mutableStateOf(false) }
    var protEmailCode by remember { mutableStateOf("") }

    LaunchedEffect(profile) {
        profile?.let {
            name = it.username ?: ""
            height = it.height?.toString() ?: ""
            weight = it.weight?.toString() ?: ""
            birthDate = it.birth_date ?: ""
            phone = it.phone ?: ""
            gender = it.gender ?: ""
            protEmail = it.prot_email ?: ""
            email = it.email ?: ""
            isProtEmailVerified = !it.prot_email.isNullOrBlank()
        }
    }
    // 문자열 리소스화
    val editprofilephoto = stringResource(R.string.editprofilephoto)
    val editText = stringResource(R.string.edit)

    // 문자열 리소스
    val emailText = stringResource(R.string.email)
    val guardianEmailText = stringResource(R.string.guardianemail)
    val nameText = stringResource(R.string.name)
    val heightText = stringResource(R.string.height)
    val weightText = stringResource(R.string.weight)
    val birthText = stringResource(R.string.birth)
    val genderText = stringResource(R.string.gender)
    val phoneNumberPlaceholderText = stringResource(R.string.phone_number_placeholder)
    val editDone = stringResource(R.string.edit_done)
    val birthExampleText = stringResource(R.string.birth_example)

    val context = LocalContext.current
    val sendText = stringResource(R.string.send)
    val sentText = stringResource(R.string.sent)
    val verificationText = stringResource(R.string.verification)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                EditProfileEvent.SaveSuccess -> {
                    myPageVm.refreshProfile()
                    Toast.makeText(context, "저장되었습니다", Toast.LENGTH_SHORT).show()
                    onDone()
                }
                EditProfileEvent.SaveFailed ->
                    Toast.makeText(context, "저장 실패", Toast.LENGTH_SHORT).show()
                EditProfileEvent.LoadFailed ->
                    Toast.makeText(context, "프로필 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            if (hasName) {
                ReadonlyField(nameText, name)
            } else {
                EditableField(nameText, name) { name = it }
            }

            EditableField(heightText, height) { height = it }
            EditableField(weightText, weight) { weight = it }

            fun isValidBirthFormat(v: String) =
                Regex("""^\d{4}-\d{2}-\d{2}$""").matches(v)
            val hasValidBirth = isValidBirthFormat(birthDate)
            if (hasValidBirth) {
                ReadonlyField(birthText, birthDate)
            } else {
                EditableField(
                    label = "${birthText} $birthExampleText",
                    value = birthDate,
                    onValueChange = { input ->
                        birthDate = input
                    }
                )
            }
            /* 1201 18:07 seok 코드
            if (isValidBirthFormat(birthDate))
                ReadonlyField(stringResource(R.string.birth), birthDate)
            else
                EditableField(
                    stringResource(R.string.birth) + " " + stringResource(R.string.birth_example),
                    birthDate
                ) { birthDate = it }
             */
            if (hasGender) {
                ReadonlyField(genderText, gender)
            } else {
                AuthGenderDropdown(
                    value = gender,
                    onValueChange = { gender = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (isLocal) {
                ReadonlyField(emailText, email)
            }
            EditableField(phoneNumberPlaceholderText, phone) { phone = it }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("보호자 이메일", fontSize = 14.sp, color = Color(0xff3b566e))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = protEmail,
                    onValueChange = {
                        protEmail = it
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

                Button(
                    onClick = {
                        if (protEmail.isNotBlank()) {

                            //  1202 임시 테스트 - 보호자이메일 aaa@aaa.com 이면 테스트 인증코드 1234
                            if (protEmail == "aaa@aaa.com") {
                                isProtEmailSent = true
                                isProtEmailVerified = false
                                protEmailCode = ""
                                Toast.makeText(context, "[테스트] 인증코드 전송됨 (코드는 1234)", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // 실제코드
                            viewModel.sendEmailCode(protEmail)
                            isProtEmailSent = true
                            isProtEmailVerified = false
                            protEmailCode = ""
                            Toast.makeText(context, "인증코드 전송됨", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "이메일 입력", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isProtEmailVerified,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    // 전송됨 상태이면 sentText, 아니면 sendText
                    Text(text = if (isProtEmailSent) sentText else sendText, fontSize = 14.sp)
                }
            }
        }

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

                    Button(
                        onClick = {

                            //  1202 임시 테스트 - 보호자이메일 aaa@aaa.com 이면 테스트 인증코드 1234
                            if (protEmail == "aaa@aaa.com" && protEmailCode == "1234") {
                                isProtEmailVerified = true
                                isProtEmailSent = false
                                Toast.makeText(context, "[테스트] 보호자 이메일 인증 성공", Toast.LENGTH_SHORT).show()
                                return@Button
                            }


                            viewModel.verifyEmailCode(protEmail, protEmailCode) { ok ->
                                if (ok) {
                                    isProtEmailVerified = true
                                    isProtEmailSent = false
                                    Toast.makeText(context, "인증 성공", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "인증 실패", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text(text = verificationText, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (protEmail.isNotBlank() && !isProtEmailVerified) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.primary
                )
                .clickable {
                    if (protEmail.isNotBlank() && !isProtEmailVerified) {
                        Toast.makeText(context, "보호자 이메일 인증 필요", Toast.LENGTH_SHORT).show()
                        return@clickable
                    }
                    viewModel.saveProfile(
                        username = name,
                        heightText = height,
                        weightText = weight,
                        ageText = birthDate,
                        email = email,
                        phone = phone,
                        prot_email = protEmail,
                        gender = gender
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.save),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = editDone,
                    color = MaterialTheme.colorScheme.surface,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface)
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
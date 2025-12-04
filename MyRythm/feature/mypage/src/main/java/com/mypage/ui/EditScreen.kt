package com.mypage.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mypage.viewmodel.EditProfileViewModel
import com.mypage.viewmodel.MyPageViewModel
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.components.AppInputField
import com.shared.ui.components.AuthGenderDropdown
import com.shared.ui.theme.AppFieldHeight
import com.shared.ui.theme.AppTheme

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

    // 문자열 리소스
    val emailText = stringResource(R.string.email)
    val nameText = stringResource(R.string.name)
    val heightText = stringResource(R.string.height)
    val weightText = stringResource(R.string.weight)
    val birthText = stringResource(R.string.birth)
    val genderText = stringResource(R.string.gender)
    val phoneNumberPlaceholderText = stringResource(R.string.phone_number_placeholder)
    val editDone = stringResource(R.string.edit_done)
    val birthExampleText = stringResource(R.string.birth_example)
    val sendText = stringResource(R.string.send)
    val sentText = stringResource(R.string.sent)
    val verificationText = stringResource(R.string.verification)
    val guardianEmailText = stringResource(R.string.guardianemail)
    val verificationCodeText = stringResource(R.string.verification_code)

    val context = LocalContext.current

    AppTheme {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 30.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {

            if (hasName) {
                AppInputField(
                    value = name,
                    onValueChange = {},
                    label = nameText,
                    readOnly = true,
                    outlined = true,
                    singleLine = true
                )
            } else {
                AppInputField(
                    value = name,
                    onValueChange = { name = it },
                    label = nameText,
                    outlined = true,
                    singleLine = true
                )
            }

            AppInputField(
                value = height,
                onValueChange = { height = it },
                label = heightText,
                outlined = true,
                singleLine = true
            )

            AppInputField(
                value = weight,
                onValueChange = { weight = it },
                label = weightText,
                outlined = true,
                singleLine = true
            )

            fun isValidBirthFormat(v: String) =
                Regex("""^\d{4}-\d{2}-\d{2}$""").matches(v)

            val hasValidBirth = isValidBirthFormat(birthDate)

            if (hasValidBirth) {
                AppInputField(
                    value = birthDate,
                    onValueChange = {},
                    label = birthText,
                    readOnly = true,
                    outlined = true,
                    singleLine = true
                )
            } else {
                AppInputField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = "$birthText $birthExampleText",
                    outlined = true,
                    singleLine = true
                )
            }

            if (hasGender) {
                AppInputField(
                    value = gender,
                    onValueChange = {},
                    label = genderText,
                    readOnly = true,
                    outlined = true,
                    singleLine = true
                )
            } else {
                AuthGenderDropdown(
                    value = gender,
                    onValueChange = { gender = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isLocal) {
                AppInputField(
                    value = email,
                    onValueChange = {},
                    label = emailText,
                    readOnly = true,
                    outlined = true,
                    singleLine = true
                )
            }

            AppInputField(
                value = phone,
                onValueChange = { phone = it },
                label = phoneNumberPlaceholderText,
                outlined = true,
                singleLine = true
            )

            AppInputField(
                value = protEmail,
                onValueChange = {
                    protEmail = it
                    isProtEmailVerified = false
                    isProtEmailSent = false
                },
                label = guardianEmailText,
                outlined = true,
                singleLine = true,
                trailingContent = {
                    AppButton(
                        text = if (isProtEmailSent) sentText else sendText,
                        height = AppFieldHeight,
                        width = 80.dp,
                        onClick = {
                            if (protEmail.isNotBlank()) {

                                // 1202 임시 테스트 - 보호자 이메일 aaa@aaa.com 은 테스트 코드 1234
                                if (protEmail == "aaa@aaa.com") {
                                    isProtEmailSent = true
                                    isProtEmailVerified = false
                                    protEmailCode = ""
                                    Toast.makeText(
                                        context,
                                        "[테스트] 인증코드 전송됨 (코드는 1234)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@AppButton
                                }

                                // 실제코드
                                /*viewModel.sendEmailCode(protEmail)
                            isProtEmailSent = true
                            isProtEmailVerified = false
                            protEmailCode = ""
                            Toast.makeText(context, "인증코드 전송됨", Toast.LENGTH_SHORT).show()*/
                            } else {
                                Toast.makeText(context, "이메일 입력", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            )

            if (isProtEmailSent && !isProtEmailVerified) {
                AppInputField(
                    value = protEmailCode,
                    onValueChange = { protEmailCode = it },
                    label = verificationCodeText,
                    outlined = true,
                    singleLine = true,
                    trailingContent = {
                        AppButton(
                            text = verificationText,
                            height = AppFieldHeight,
                            width = 80.dp,
                            onClick = {
                                // 1202 임시 테스트 - 보호자이메일 aaa@aaa.com 은 인증코드 1234
                                if (protEmail == "aaa@aaa.com" && protEmailCode == "1234") {
                                    isProtEmailVerified = true
                                    isProtEmailSent = false
                                    Toast.makeText(
                                        context,
                                        "[테스트] 보호자 이메일 인증 성공",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@AppButton
                                }

                                // 실제코드
                                /*viewModel.verifyEmailCode(protEmail, protEmailCode) { ok ->
                                if (ok) {
                                    isProtEmailVerified = true
                                    isProtEmailSent = false
                                    Toast.makeText(context, "인증 성공", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "인증 실패", Toast.LENGTH_SHORT).show()
                                }
                            }*/
                            }
                        )
                    }
                )
            }

            /* Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (protEmail.isNotBlank() && !isProtEmailVerified)
                        MaterialTheme.colorScheme.surfaceVariant
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
        }*/

            AppButton(
                text = editDone,
                onClick = {
                    if (protEmail.isNotBlank() && !isProtEmailVerified) {
                        Toast.makeText(context, "보호자 이메일 인증 필요", Toast.LENGTH_SHORT).show()
                        return@AppButton
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
                modifier = Modifier.fillMaxWidth(),
                height = AppFieldHeight,
                shape = MaterialTheme.shapes.medium,
                backgroundColor = if (protEmail.isNotBlank() && !isProtEmailVerified)
                    MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.surface,
                content = {
                    Image(
                        painter = painterResource(R.drawable.save),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

        }
    }
}


/*
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

    Spacer(Modifier.width(8.dp))

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
        Text(text = if (isProtEmailSent) sentText else sendText, fontSize = 14.sp)
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
            Spacer(Modifier.width(8.dp))

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

Spacer(Modifier.height(8.dp))

Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(
            if (protEmail.isNotBlank() && !isProtEmailVerified)
                MaterialTheme.colorScheme.surfaceVariant
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
*/
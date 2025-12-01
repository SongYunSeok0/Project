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
import com.mypage.viewmodel.MyPageViewModel
import com.shared.ui.components.AuthGenderDropdown

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

    var name by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var protEmail by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

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

    val context = LocalContext.current

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

            if (name.isNotBlank())
                ReadonlyField(stringResource(R.string.name), name)
            else
                EditableField(stringResource(R.string.name), name) { name = it }

            EditableField(stringResource(R.string.height), height) { height = it }
            EditableField(stringResource(R.string.weight), weight) { weight = it }

            fun isValidBirthFormat(v: String) =
                Regex("""^\d{4}-\d{2}-\d{2}$""").matches(v)

            if (isValidBirthFormat(birthDate))
                ReadonlyField(stringResource(R.string.birth), birthDate)
            else
                EditableField(
                    stringResource(R.string.birth) + " " + stringResource(R.string.birth_example),
                    birthDate
                ) { birthDate = it }

            if (gender.isNotBlank())
                ReadonlyField(stringResource(R.string.gender), gender)
            else
                AuthGenderDropdown(
                    value = gender,
                    onValueChange = { gender = it },
                    modifier = Modifier.fillMaxWidth()
                )

            if (isLocal)
                ReadonlyField(stringResource(R.string.email), email)

            EditableField(stringResource(R.string.phone_number_placeholder), phone) { phone = it }
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
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(
                        text = if (isProtEmailSent) stringResource(R.string.sent)
                        else stringResource(R.string.send),
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (isProtEmailSent && !isProtEmailVerified) {

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                Text("인증번호", fontSize = 14.sp, color = Color(0xff3b566e))

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
                        Text(stringResource(R.string.verification), fontSize = 14.sp)
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
                    if (protEmail.isNotBlank() && !isProtEmailVerified) Color.Gray
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
                    text = stringResource(R.string.edit_done),
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
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
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

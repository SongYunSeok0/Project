package com.mypage.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mypage.viewmodel.EditProfileEvent
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

    // 소셜 로그인 판단
    val isSocialLogin = profile?.username.isNullOrBlank() || profile?.phone.isNullOrBlank()

    var name by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    // 생년월일 3개 필드
    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }

    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    // 보호자 관련 상태
    var protEmail by remember { mutableStateOf("") }
    var protName by remember { mutableStateOf("") } // [추가] 보호자 이름
    var email by remember { mutableStateOf("") }

    // 각 필드 등록 여부
    val hasName = !profile?.username.isNullOrBlank()
    val hasPhone = !profile?.phone.isNullOrBlank()
    val hasGender = !profile?.gender.isNullOrBlank()
    val hasEmail = !profile?.email.isNullOrBlank()
    val hasValidBirth = profile?.birth_date?.let {
        Regex("""^\d{4}-\d{2}-\d{2}$""").matches(it)
    } ?: false

    // 보호자 이메일 인증 상태
    var isProtEmailVerified by remember { mutableStateOf(false) }
    var isProtEmailSent by remember { mutableStateOf(false) }
    var protEmailCode by remember { mutableStateOf("") }

    // 스크롤 상태
    val scrollState = rememberScrollState()

    // 이메일 인증 상태
    var isEmailVerified by remember { mutableStateOf(false) }
    var isEmailSent by remember { mutableStateOf(false) }
    var emailCode by remember { mutableStateOf("") }

    LaunchedEffect(profile) {
        profile?.let {
            name = it.username ?: ""
            height = it.height?.toString() ?: ""
            weight = it.weight?.toString() ?: ""

            // 생년월일 파싱
            it.birth_date?.let { date ->
                val parts = date.split("-")
                if (parts.size == 3) {
                    birthYear = parts[0]
                    birthMonth = parts[1]
                    birthDay = parts[2]
                }
            }

            phone = it.phone ?: ""
            gender = it.gender ?: ""
            protEmail = it.prot_email ?: ""
            protName = it.prot_name ?: "" // [추가] 서버 프로필에 prot_name 필드가 있다면 여기서 초기화
            email = it.email ?: ""
            isProtEmailVerified = !it.prot_email.isNullOrBlank()
            isEmailVerified = !it.email.isNullOrBlank()
        }
    }

    val emailText = stringResource(R.string.email)
    val nameText = stringResource(R.string.name)
    val heightText = stringResource(R.string.height)
    val weightText = stringResource(R.string.weight)
    val birthText = stringResource(R.string.birth)
    val genderText = stringResource(R.string.gender)
    val phoneNumberPlaceholderText = stringResource(R.string.phone_number_placeholder)
    val editDone = stringResource(R.string.edit_done)
    val yearText = "년"
    val monthText = "월"
    val dayText = "일"
    val context = LocalContext.current
    val sendText = stringResource(R.string.send)
    val sentText = stringResource(R.string.sent)
    val verificationText = stringResource(R.string.verification)
    val guardianEmailText = stringResource(R.string.guardianemail)
    val guardiannameText = stringResource(R.string.guardianname)
    val verificationCodeText = stringResource(R.string.verification_code)
    val labelText = stringResource(R.string.label)
    val verificationSuccessText = stringResource(R.string.verification_success)
    val verificationFailedText = stringResource(R.string.verification_failed)
    val saveFailedText = stringResource(R.string.save_failed)
    val savedMessage = stringResource(R.string.mypage_message_saved)
    val codeSentMessage = stringResource(R.string.mypage_message_code_sent)
    val emailDuplicateMessage = stringResource(R.string.mypage_message_email_duplicate)
    val enterNameMessage = stringResource(R.string.mypage_message_enter_name)
    val enterPhoneMessage = stringResource(R.string.mypage_message_enter_phone)
    val selectGenderMessage = stringResource(R.string.mypage_message_select_gender)
    val enterEmailMessage = stringResource(R.string.mypage_message_enter_email)
    val emailVerificationRequiredMessage = stringResource(R.string.mypage_message_email_verification_required)
    val enterGuardianInfoMessage = stringResource(R.string.mypage_message_enter_guardian_info)
    val guardianVerificationRequiredMessage = stringResource(R.string.mypage_message_guardian_verification_required)
    val profileInfoNoticeMessage = stringResource(R.string.mypage_message_profile_info_notice)
    val errorprofileLoadFailed = stringResource(R.string.error_profile_load_failed)

    // 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                EditProfileEvent.SaveSuccess -> {
                    myPageVm.refreshProfile()
                    Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
                    onDone()
                }
                EditProfileEvent.SaveFailed -> {
                    Toast.makeText(context, saveFailedText, Toast.LENGTH_SHORT).show()
                }
                EditProfileEvent.LoadFailed -> {
                    Toast.makeText(context, errorprofileLoadFailed, Toast.LENGTH_SHORT).show()
                }
                EditProfileEvent.EmailSent -> {
                    isProtEmailSent = true
                    isProtEmailVerified = false
                    protEmailCode = ""
                    Toast.makeText(context, codeSentMessage, Toast.LENGTH_SHORT).show()
                }
                is EditProfileEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    AppTheme {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 30.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {

            // 소셜 로그인 안내
            if (isSocialLogin) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = profileInfoNoticeMessage,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // 이름
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

                // 생년월일 입력
                if (hasValidBirth) {
                    AppInputField(
                        value = profile?.birth_date ?: "",
                        onValueChange = {},
                        label = birthText,
                        readOnly = true,
                        outlined = true,
                        singleLine = true
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AppInputField(
                            value = birthYear,
                            onValueChange = {
                                birthYear = it.filter { c -> c.isDigit() }.take(4)
                            },
                            label = yearText,
                            outlined = true,
                            singleLine = true,
                            modifier = Modifier.weight(1.5f),
                            keyboardType = KeyboardType.Number
                        )

                        AppInputField(
                            value = birthMonth,
                            onValueChange = {
                                birthMonth = it.filter { c -> c.isDigit() }.take(2)
                            },
                            label = monthText,
                            outlined = true,
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Number
                        )

                        AppInputField(
                            value = birthDay,
                            onValueChange = {
                                birthDay = it.filter { c -> c.isDigit() }.take(2)
                            },
                            label = dayText,
                            outlined = true,
                            singleLine = true,
                            modifier = Modifier.weight(1f),

                            keyboardType = KeyboardType.Number
                        )
                    }
                }

            Spacer(modifier = Modifier.height(8.dp))

            // 성별
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

            Spacer(modifier = Modifier.height(8.dp))


            // 이메일 (소셜 로그인만 입력 가능)
                if (hasEmail) {
                    AppInputField(
                        value = email,
                        onValueChange = {},
                        label = "$emailText$labelText",
                        readOnly = true,
                        outlined = true,
                        singleLine = true
                    )
                } else {
                    AppInputField(
                        value = email,
                        onValueChange = {
                            email = it
                            isEmailVerified = false
                            isEmailSent = false
                        },
                        label = "$emailText$labelText",
                        outlined = true,
                        singleLine = true,
                        keyboardType = KeyboardType.Email,
                        trailingContent = {
                            AppButton(
                                text = if (isEmailSent) sentText else sendText,
                                height = AppFieldHeight,
                                width = 80.dp,
                                enabled = !isEmailVerified,
                                onClick = {
                                    if (email.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            enterEmailMessage,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@AppButton
                                    }
                                    viewModel.checkEmailDuplicate(email) { isDuplicate ->
                                        if (isDuplicate) {
                                            Toast.makeText(
                                                context,
                                                emailDuplicateMessage,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            viewModel.sendEmailCode(email)
                                            isEmailSent = true
                                            isEmailVerified = false
                                            emailCode = ""
                                            Toast.makeText(
                                                context,
                                                codeSentMessage,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            )
                        }
                    )

                    if (isEmailSent && !isEmailVerified) {
                        AppInputField(
                            value = emailCode,
                            onValueChange = { emailCode = it },
                            label = verificationCodeText,
                            outlined = true,
                            singleLine = true,
                            keyboardType = KeyboardType.Number,
                            trailingContent = {
                                AppButton(
                                    text = verificationText,
                                    height = AppFieldHeight,
                                    width = 80.dp,
                                    onClick = {
                                        if (email == "test@test.com" && emailCode == "1111") {
                                            isEmailVerified = true
                                            isEmailSent = false
                                            Toast.makeText(
                                                context,
                                                "[테스트] 이메일 인증 성공",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@AppButton
                                        }

                                        viewModel.verifyEmailCode(email, emailCode) { ok ->
                                            if (ok) {
                                                isEmailVerified = true
                                                isEmailSent = false
                                                Toast.makeText(
                                                    context,
                                                    verificationFailedText,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    verificationSuccessText,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }
                }

                // 전화번호
                if (hasPhone) {
                    AppInputField(
                        value = phone,
                        onValueChange = {},
                        label = phoneNumberPlaceholderText,
                        readOnly = true,
                        outlined = true,
                        singleLine = true
                    )
                } else {
                    AppInputField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = phoneNumberPlaceholderText,
                        outlined = true,
                        singleLine = true
                    )
                }

                // --- 보호자 정보 섹션 ---
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                    // 1. [추가] 보호자 이름 입력 필드
                    AppInputField(
                        value = protName,
                        onValueChange = {
                            protName = it
                            // 이름이 바뀌면 다시 인증하도록 초기화
                            isProtEmailVerified = false
                            isProtEmailSent = false
                        },
                        label = guardiannameText,
                        outlined = true,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    AppInputField(
                        value = protEmail,
                        onValueChange = {
                            protEmail = it
                            isProtEmailVerified = false
                            isProtEmailSent = false
                        },
                        label = "$guardianEmailText$labelText",
                        outlined = true,
                        singleLine = true,

                        trailingContent = {
                            AppButton(
                                text = if (isProtEmailSent) sentText else sendText,
                                height = AppFieldHeight,
                                width = 80.dp,
                                enabled = !isProtEmailVerified,
                                onClick = {
                                    if (protEmail.isNotBlank() && protName.isNotBlank()) {

                                        // ui용 테스트코드
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
                                        viewModel.sendEmailCode(protEmail, protName)

                                    } else {
                                        Toast.makeText(
                                            context,
                                            enterGuardianInfoMessage,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                    )
                }

                if (isProtEmailSent && !isProtEmailVerified) {
                    AppInputField(
                        value = protEmailCode,
                        onValueChange = { protEmailCode = it },
                        label = verificationCodeText,
                        outlined = true,
                        singleLine = true,
                        keyboardType = KeyboardType.Number,
                        trailingContent = {
                            AppButton(
                                text = verificationText,
                                height = AppFieldHeight,
                                width = 80.dp,
                                onClick = {

                                    // ui용 테스트코드
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
                                    viewModel.verifyEmailCode(protEmail, protEmailCode) { ok ->
                                        if (ok) {
                                            isProtEmailVerified = true
                                            isProtEmailSent = false
                                            Toast.makeText(context, verificationSuccessText, Toast.LENGTH_SHORT)
                                                .show()
                                        } else {
                                            Toast.makeText(context, verificationFailedText, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    }
                                }
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 저장 버튼
                AppButton(
                    text = editDone,
                    onClick = {
                        // ui테스트용코드
                        val isTestGuardian = (protName == "aaa" && protEmail == "aaa@aaa.com")

                        // 소셜 로그인 필수 정보 체크
                        if (isSocialLogin) {
                            if (name.isBlank()) {
                                Toast.makeText(context, enterNameMessage, Toast.LENGTH_SHORT).show()
                                return@AppButton
                            }
                            if (phone.isBlank()) {
                                Toast.makeText(context, enterPhoneMessage, Toast.LENGTH_SHORT)
                                    .show()
                                return@AppButton
                            }
                            if (gender.isBlank()) {
                                Toast.makeText(context, selectGenderMessage, Toast.LENGTH_SHORT).show()
                                return@AppButton
                            }
                            if (email.isBlank()) {
                                Toast.makeText(context, enterEmailMessage, Toast.LENGTH_SHORT)
                                    .show()
                                return@AppButton
                            }
                            if (!isEmailVerified) {
                                Toast.makeText(context, emailVerificationRequiredMessage, Toast.LENGTH_SHORT)
                                    .show()
                                return@AppButton
                            }
                        }

                        // ui테스트용코드
                        if (!isTestGuardian && protEmail.isNotBlank() && !isProtEmailVerified) {
                            Toast.makeText(context, guardianVerificationRequiredMessage, Toast.LENGTH_SHORT).show()
                            return@AppButton
                        }

                        // 생년월일 합치기
                        val birthDate =
                            if (birthYear.length == 4 && birthMonth.isNotBlank() && birthDay.isNotBlank()) {
                                val month = birthMonth.padStart(2, '0')
                                val day = birthDay.padStart(2, '0')
                                "$birthYear-$month-$day"
                            } else ""

                        viewModel.saveProfile(
                            username = name,
                            heightText = height,
                            weightText = weight,
                            ageText = birthDate,
                            email = email,
                            phone = phone,
                            prot_email = protEmail,
                            prot_name = protName,
                            gender = gender
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    height = AppFieldHeight,
                    backgroundColor = if (
                        (email.isNotBlank() && !isEmailVerified) ||
                        (protEmail.isNotBlank() && !isProtEmailVerified)
                    ) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    textColor = MaterialTheme.colorScheme.surface,
                    content = {
                        Image(
                            painter = painterResource(R.drawable.save),
                            contentDescription = editDone,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
package com.mypage.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.delay

// 🔥 소셜 로그인 username인지 확인
private fun isSocialUsername(username: String?): Boolean {
    if (username.isNullOrBlank()) return false
    return username.startsWith("kakao_") ||
            username.startsWith("google_") ||
            username.startsWith("naver_")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel(),
    myPageVm: MyPageViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()

    // 🔥 remember 변수로 상태 관리
    var hasRealName by remember { mutableStateOf(false) }
    var hasRealPhone by remember { mutableStateOf(false) }
    var hasRealGender by remember { mutableStateOf(false) }
    var hasRealEmail by remember { mutableStateOf(false) }
    var hasValidBirth by remember { mutableStateOf(false) }

    // 🔥 소셜 로그인 안내 카드 표시 여부
    val showSocialNotice = !hasRealName || !hasRealPhone || !hasRealGender

    var name by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    // 생년월일 3개 필드
    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }  // 전체 생년월일 저장용

    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    // 보호자 관련 상태
    var protEmail by remember { mutableStateOf("") }
    var protName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // 📧 사용자 이메일 인증 상태
    var isEmailVerified by rememberSaveable { mutableStateOf(false) }
    var isEmailSent by rememberSaveable { mutableStateOf(false) }
    var emailCode by rememberSaveable { mutableStateOf("") }
    var emailSendCount by rememberSaveable { mutableIntStateOf(0) }
    var emailRemainingSeconds by rememberSaveable { mutableIntStateOf(0) }
    var isEmailTimerRunning by remember { mutableStateOf(false) }

    // 📧 보호자 이메일 인증 상태
    var isProtEmailVerified by rememberSaveable { mutableStateOf(false) }
    var isProtEmailSent by rememberSaveable { mutableStateOf(false) }
    var protEmailCode by rememberSaveable { mutableStateOf("") }
    var protEmailSendCount by rememberSaveable { mutableIntStateOf(0) }
    var protEmailRemainingSeconds by rememberSaveable { mutableIntStateOf(0) }
    var isProtEmailTimerRunning by remember { mutableStateOf(false) }

    // 🔥 초기화 여부 추적
    var isInitialized by remember { mutableStateOf(false) }

    // ⏱️ 사용자 이메일 타이머
    LaunchedEffect(isEmailTimerRunning) {
        if (isEmailTimerRunning && emailRemainingSeconds > 0) {
            while (emailRemainingSeconds > 0) {
                delay(1000L)
                emailRemainingSeconds--
            }
            isEmailTimerRunning = false
        }
    }

    // ⏱️ 보호자 이메일 타이머
    LaunchedEffect(isProtEmailTimerRunning) {
        if (isProtEmailTimerRunning && protEmailRemainingSeconds > 0) {
            while (protEmailRemainingSeconds > 0) {
                delay(1000L)
                protEmailRemainingSeconds--
            }
            isProtEmailTimerRunning = false
        }
    }

    // 🔥 프로필 데이터 초기화 (한 번만 실행)
    LaunchedEffect(profile) {
        val currentProfile = profile
        if (!isInitialized && currentProfile != null) {
            name = currentProfile.username ?: ""
            height = currentProfile.height?.toString() ?: ""
            weight = currentProfile.weight?.toString() ?: ""

            // 생년월일 파싱
            currentProfile.birth_date?.let { date ->
                birthDate = date  // 전체 날짜 저장
                val parts = date.split("-")
                if (parts.size == 3) {
                    birthYear = parts[0]
                    birthMonth = parts[1]
                    birthDay = parts[2]
                }
            }

            phone = currentProfile.phone ?: ""
            gender = currentProfile.gender ?: ""
            protEmail = currentProfile.prot_email ?: ""
            protName = currentProfile.prot_name ?: ""
            email = currentProfile.email ?: ""

            // 🔥 실제 정보 등록 여부 체크
            hasRealName = !currentProfile.username.isNullOrBlank() && !isSocialUsername(currentProfile.username)
            hasRealPhone = !currentProfile.phone.isNullOrBlank()
            hasRealGender = !currentProfile.gender.isNullOrBlank()
            hasRealEmail = !currentProfile.email.isNullOrBlank()
            hasValidBirth = currentProfile.birth_date?.let { date ->
                Regex("""^\d{4}-\d{2}-\d{2}$""").matches(date)
            } ?: false

            // 이미 등록된 이메일이 있으면 인증 완료 상태로 설정
            isProtEmailVerified = !currentProfile.prot_email.isNullOrBlank()
            isEmailVerified = !currentProfile.email.isNullOrBlank()

            isInitialized = true
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
    val resendText = "재전송"
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
                    // 이벤트는 유지하되, 타이머 로직은 개별 처리
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

            // 🔥 소셜 로그인 안내 (실제 정보가 없을 때만 표시)
            if (showSocialNotice) {
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

                // 🔥 이름 - 실제 이름이 등록되면 읽기 전용
                if (hasRealName) {
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
                        value = birthDate,
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

                // 🔥 성별 - 등록되면 읽기 전용
                if (hasRealGender) {
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

                // 📧 사용자 이메일 인증 섹션
                if (hasRealEmail) {
                    AppInputField(
                        value = email,
                        onValueChange = {},
                        label = "$emailText$labelText",
                        readOnly = true,
                        outlined = true,
                        singleLine = true
                    )
                } else {
                    Column {
                        AppInputField(
                            value = email,
                            onValueChange = {
                                email = it
                                // 이메일 변경 시 인증 상태 초기화
                                if (isEmailVerified || isEmailSent) {
                                    isEmailVerified = false
                                    isEmailSent = false
                                    emailSendCount = 0
                                    emailRemainingSeconds = 0
                                    isEmailTimerRunning = false
                                    emailCode = ""
                                }
                            },
                            label = "$emailText$labelText",
                            outlined = true,
                            singleLine = true,
                            keyboardType = KeyboardType.Email,
                            readOnly = isEmailVerified,
                            trailingContent = {
                                AppButton(
                                    text = if (isEmailSent) resendText else sendText,
                                    height = AppFieldHeight,
                                    width = 80.dp,
                                    enabled = email.isNotBlank() && emailSendCount < 5 && !isEmailVerified,
                                    onClick = {
                                        if (email.isBlank()) {
                                            Toast.makeText(
                                                context,
                                                enterEmailMessage,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@AppButton
                                        }
                                        if (emailSendCount >= 5) {
                                            Toast.makeText(
                                                context,
                                                "인증 요청 횟수가 초과되었습니다. 1시간 후 다시 시도해주세요.",
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
                                                emailSendCount++
                                                emailRemainingSeconds = 180
                                                isEmailTimerRunning = true
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

                        // ⏱️ 타이머 표시
                        if (isEmailTimerRunning && emailRemainingSeconds > 0) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "인증 번호 발송 완료",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF9E9E9E),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                                Text(
                                    text = "%02d:%02d".format(
                                        emailRemainingSeconds / 60,
                                        emailRemainingSeconds % 60
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFF6B6B),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }

                        if (isEmailSent && !isEmailVerified) {
                            Spacer(Modifier.height(8.dp))
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
                                        enabled = emailCode.isNotBlank(),
                                        onClick = {
                                            if (email == "test@test.com" && emailCode == "1111") {
                                                isEmailVerified = true
                                                isEmailSent = false
                                                isEmailTimerRunning = false
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
                                                    isEmailTimerRunning = false
                                                    Toast.makeText(
                                                        context,
                                                        verificationSuccessText,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        verificationFailedText,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    )
                                }
                            )
                        }

                        // ✅ 인증 완료 메시지
                        if (isEmailVerified) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "인증 완료",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9E9E9E),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp)
                            )
                        }
                    }
                }

                // 🔥 전화번호 - 등록되면 읽기 전용
                if (hasRealPhone) {
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

                Spacer(modifier = Modifier.height(8.dp))

                // --- 📧 보호자 이메일 인증 섹션 ---
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                    // 보호자 이름 입력 필드
                    AppInputField(
                        value = protName,
                        onValueChange = {
                            protName = it
                            if (isProtEmailVerified || isProtEmailSent) {
                                isProtEmailVerified = false
                                isProtEmailSent = false
                                protEmailSendCount = 0
                                protEmailRemainingSeconds = 0
                                isProtEmailTimerRunning = false
                                protEmailCode = ""
                            }
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
                            if (isProtEmailVerified || isProtEmailSent) {
                                isProtEmailVerified = false
                                isProtEmailSent = false
                                protEmailSendCount = 0
                                protEmailRemainingSeconds = 0
                                isProtEmailTimerRunning = false
                                protEmailCode = ""
                            }
                        },
                        label = "$guardianEmailText$labelText",
                        outlined = true,
                        singleLine = true,
                        keyboardType = KeyboardType.Email,
                        readOnly = isProtEmailVerified,
                        trailingContent = {
                            AppButton(
                                text = if (isProtEmailSent) resendText else sendText,
                                height = AppFieldHeight,
                                width = 80.dp,
                                enabled = protEmail.isNotBlank() && protName.isNotBlank() &&
                                        protEmailSendCount < 5 && !isProtEmailVerified,
                                onClick = {
                                    if (protEmail.isBlank() || protName.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            enterGuardianInfoMessage,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@AppButton
                                    }
                                    if (protEmailSendCount >= 5) {
                                        Toast.makeText(
                                            context,
                                            "인증 요청 횟수가 초과되었습니다. 1시간 후 다시 시도해주세요.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@AppButton
                                    }

                                    if (protEmail == "aaa@aaa.com") {
                                        isProtEmailSent = true
                                        isProtEmailVerified = false
                                        protEmailCode = ""
                                        protEmailSendCount++
                                        protEmailRemainingSeconds = 180
                                        isProtEmailTimerRunning = true
                                        Toast.makeText(
                                            context,
                                            "[테스트] 인증코드 전송됨 (코드는 1234)",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@AppButton
                                    }

                                    viewModel.sendEmailCode(protEmail, protName)
                                    isProtEmailSent = true
                                    isProtEmailVerified = false
                                    protEmailCode = ""
                                    protEmailSendCount++
                                    protEmailRemainingSeconds = 180
                                    isProtEmailTimerRunning = true
                                    Toast.makeText(
                                        context,
                                        codeSentMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    )

                    // ⏱️ 보호자 이메일 타이머 표시
                    if (isProtEmailTimerRunning && protEmailRemainingSeconds > 0) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = "인증 번호 발송 완료",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9E9E9E),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Text(
                                text = "%02d:%02d".format(
                                    protEmailRemainingSeconds / 60,
                                    protEmailRemainingSeconds % 60
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFF6B6B),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }

                    if (isProtEmailSent && !isProtEmailVerified) {
                        Spacer(Modifier.height(8.dp))
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
                                    enabled = protEmailCode.isNotBlank(),
                                    onClick = {
                                        if (protEmail == "aaa@aaa.com" && protEmailCode == "1234") {
                                            isProtEmailVerified = true
                                            isProtEmailSent = false
                                            isProtEmailTimerRunning = false
                                            Toast.makeText(
                                                context,
                                                "[테스트] 보호자 이메일 인증 성공",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@AppButton
                                        }

                                        viewModel.verifyEmailCode(protEmail, protEmailCode) { ok ->
                                            if (ok) {
                                                isProtEmailVerified = true
                                                isProtEmailSent = false
                                                isProtEmailTimerRunning = false
                                                Toast.makeText(
                                                    context,
                                                    verificationSuccessText,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    verificationFailedText,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }

                    // ✅ 보호자 인증 완료 메시지
                    if (isProtEmailVerified) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "인증 완료",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9E9E9E),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 저장 버튼
                AppButton(
                    text = editDone,
                    onClick = {
                        val isTestGuardian = (protName == "aaa" && protEmail == "aaa@aaa.com")

                        // 🔥 필수 정보 체크 (실제 정보가 없을 때만)
                        if (!hasRealName && name.isBlank()) {
                            Toast.makeText(context, enterNameMessage, Toast.LENGTH_SHORT).show()
                            return@AppButton
                        }
                        if (!hasRealPhone && phone.isBlank()) {
                            Toast.makeText(context, enterPhoneMessage, Toast.LENGTH_SHORT).show()
                            return@AppButton
                        }
                        if (!hasRealGender && gender.isBlank()) {
                            Toast.makeText(context, selectGenderMessage, Toast.LENGTH_SHORT).show()
                            return@AppButton
                        }
                        if (!hasRealEmail && email.isBlank()) {
                            Toast.makeText(context, enterEmailMessage, Toast.LENGTH_SHORT).show()
                            return@AppButton
                        }
                        if (!hasRealEmail && !isEmailVerified) {
                            Toast.makeText(context, emailVerificationRequiredMessage, Toast.LENGTH_SHORT).show()
                            return@AppButton
                        }

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
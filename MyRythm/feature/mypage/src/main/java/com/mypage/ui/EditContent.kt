package com.mypage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mypage.ui.components.EditEmailSection
import com.mypage.ui.components.EditSocialNoticeCard
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.components.AppInputField
import com.shared.ui.components.AuthGenderDropdown
import com.shared.ui.theme.AppFieldHeight
import com.shared.ui.theme.AppTheme

// 🔥 소셜 로그인 username인지 확인
private fun isSocialUsername(username: String?): Boolean {
    if (username.isNullOrBlank()) return false
    return username.startsWith("kakao_") ||
            username.startsWith("google_") ||
            username.startsWith("naver_")
}

@Composable
fun EditContent(
    modifier: Modifier = Modifier,
    initialName: String,
    initialHeight: String,
    initialWeight: String,
    initialBirthDate: String,
    initialPhone: String,
    initialGender: String,
    initialProtEmail: String,
    initialProtName: String,
    initialEmail: String,
    onSave: (String, String, String, String, String, String, String, String, String) -> Unit,
    sendEmailCode: (String, String?) -> Unit,
    verifyEmailCode: (String, String, (Boolean) -> Unit) -> Unit,
    checkEmailDuplicate: (String, (Boolean) -> Unit) -> Unit
) {
    // 🔥 remember 변수로 상태 관리
    var hasRealName by remember { mutableStateOf(false) }
    var hasRealPhone by remember { mutableStateOf(false) }
    var hasRealGender by remember { mutableStateOf(false) }
    var hasRealEmail by remember { mutableStateOf(false) }
    var hasValidBirth by remember { mutableStateOf(false) }

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

    // 🔥 소셜 로그인 안내 카드 표시 여부
    val showSocialNotice = !hasRealName || !hasRealPhone || !hasRealGender

    // 📧 보호자 이메일 인증 상태
    var isProtEmailVerified by rememberSaveable { mutableStateOf(false) }

    // 🔥 초기화 여부 추적
    var isInitialized by remember { mutableStateOf(false) }

    // 🔥 프로필 데이터 초기화 (한 번만 실행)
    LaunchedEffect(initialName) {
        if (!isInitialized && initialName.isNotEmpty()) {
            name = initialName
            height = initialHeight
            weight = initialWeight

            // 생년월일 파싱
            if (initialBirthDate.isNotEmpty()) {
                birthDate = initialBirthDate
                val parts = initialBirthDate.split("-")
                if (parts.size == 3) {
                    birthYear = parts[0]
                    birthMonth = parts[1]
                    birthDay = parts[2]
                }
            }

            phone = initialPhone
            gender = initialGender
            protEmail = initialProtEmail
            protName = initialProtName
            email = initialEmail

            // 🔥 실제 정보 등록 여부 체크
            hasRealName = initialName.isNotEmpty() && !isSocialUsername(initialName)
            hasRealPhone = initialPhone.isNotEmpty()
            hasRealGender = initialGender.isNotEmpty()
            hasRealEmail = initialEmail.isNotEmpty()
            hasValidBirth = Regex("""^\d{4}-\d{2}-\d{2}$""").matches(initialBirthDate)

            isProtEmailVerified = initialProtEmail.isNotEmpty()
            isEmailVerified = initialEmail.isNotEmpty()

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
    val guardianEmailText = stringResource(R.string.guardianemail)
    val guardiannameText = stringResource(R.string.guardianname)
    val labelText = stringResource(R.string.label)
    val profileInfoNoticeMessage = stringResource(R.string.mypage_message_profile_info_notice)

    AppTheme {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 30.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            EditSocialNoticeCard(show = showSocialNotice, message = profileInfoNoticeMessage)

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
                    EditEmailSection(
                        label = "$emailText$labelText",
                        email = email,
                        onEmailChange = { email = it; isEmailVerified = false },
                        isVerified = isEmailVerified,
                        onVerifiedChange = { isEmailVerified = it },
                        isReadOnly = hasRealEmail,
                        sendCode = { e -> sendEmailCode(e, null) },
                        verifyCode = verifyEmailCode,
                        checkEmailDuplicate = checkEmailDuplicate
                    )
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
                EditEmailSection(
                    label = "$guardianEmailText$labelText",
                    email = protEmail,
                    onEmailChange = { protEmail = it; isProtEmailVerified = false },
                    isVerified = isProtEmailVerified,
                    onVerifiedChange = { isProtEmailVerified = it },
                    isReadOnly = false,
                    protName = protName,
                    onProtNameChange = { protName = it },
                    protNameLabel = guardiannameText,
                    sendCode = { e -> sendEmailCode(e, protName) },
                    verifyCode = verifyEmailCode,
                    checkEmailDuplicate = checkEmailDuplicate
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 저장 버튼
                AppButton(
                    text = editDone,
                    onClick = {
                        val birthDateStr =
                            if (birthYear.length == 4 && birthMonth.isNotBlank() && birthDay.isNotBlank()) {
                                val month = birthMonth.padStart(2, '0')
                                val day = birthDay.padStart(2, '0')
                                "$birthYear-$month-$day"
                            } else ""

                        onSave(name, height, weight, birthDateStr, email, phone, protEmail, protName, gender)
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
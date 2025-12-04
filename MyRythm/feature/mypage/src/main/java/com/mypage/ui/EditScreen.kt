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

    // [수정] 스크롤 상태 추가
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

    val yearText = "년"
    val monthText = "월"
    val dayText = "일"

    val context = LocalContext.current
    val sendText = stringResource(R.string.send)
    val sentText = stringResource(R.string.sent)
    val verificationText = stringResource(R.string.verification)
    val guardianEmailText = stringResource(R.string.guardianemail)
    val verificationCodeText = stringResource(R.string.verification_code)

    // [수정] 이벤트 핸들링: 성공/실패 여부를 여기서 처리
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                EditProfileEvent.SaveSuccess -> {
                    myPageVm.refreshProfile()
                    Toast.makeText(context, "저장되었습니다", Toast.LENGTH_SHORT).show()
                    onDone()
                }
                EditProfileEvent.SaveFailed -> {
                    Toast.makeText(context, "저장 실패", Toast.LENGTH_SHORT).show()
                }
                EditProfileEvent.LoadFailed -> {
                    Toast.makeText(context, "프로필 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
                // 이메일 전송 성공 시 UI 업데이트 및 토스트
                EditProfileEvent.EmailSent -> {
                    isProtEmailSent = true
                    isProtEmailVerified = false
                    protEmailCode = ""
                    Toast.makeText(context, "인증코드 전송됨", Toast.LENGTH_SHORT).show()
                }
                // 에러 발생 시 토스트 (404 등)
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
                        text = "⚠️ 필수 정보를 입력해주세요\n이름, 전화번호, 성별, 이메일은 한번만 입력 가능합니다.",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
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

                // 생년월일 (3개 필드)
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
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = birthText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 년
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

                            // 월
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

                            // 일
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
                }


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

                // 이메일 (소셜 로그인만 입력 가능)
                if (hasEmail) {
                    AppInputField(
                        value = email,
                        onValueChange = {},
                        label = emailText,
                        readOnly = true,
                        outlined = true,
                        singleLine = true
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("이메일 (필수)", fontSize = 14.sp, color = Color(0xff3b566e))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 입력 필드
                            AppInputField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    isEmailVerified = false
                                    isEmailSent = false
                                },
                                label = emailText,
                                outlined = true,
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                keyboardType = KeyboardType.Email
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // 인증코드 보내기 버튼
                            AppButton(
                                text = if (isEmailSent) sentText else sendText,
                                height = AppFieldHeight,
                                width = 80.dp,
                                enabled = !isEmailVerified,
                                onClick = {
                                    if (email.isBlank()) {
                                        Toast.makeText(context, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                                        return@AppButton
                                    }

                                    // 실제 코드: 중복 체크 후 인증코드 전송
                                    viewModel.checkEmailDuplicate(email) { isDuplicate ->
                                        if (isDuplicate) {
                                            Toast.makeText(context, "이미 사용 중인 이메일입니다", Toast.LENGTH_LONG).show()
                                        } else {
                                            viewModel.sendEmailCode(email)
                                            isEmailSent = true
                                            isEmailVerified = false
                                            emailCode = ""
                                            Toast.makeText(context, "인증코드 전송됨", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // 이메일 인증번호 입력
                    if (isEmailSent && !isEmailVerified) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "인증번호", fontSize = 14.sp, color = Color(0xff3b566e))

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                // 인증코드 입력
                                AppInputField(
                                    value = emailCode,
                                    onValueChange = { emailCode = it },
                                    label = verificationCodeText,
                                    outlined = true,
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    keyboardType = KeyboardType.Number
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // 인증 버튼
                                AppButton(
                                    text = verificationText,
                                    height = AppFieldHeight,
                                    width = 80.dp,
                                    onClick = {

                                        // 테스트 코드
                                        if (email == "test@test.com" && emailCode == "1111") {
                                            isEmailVerified = true
                                            isEmailSent = false
                                            Toast.makeText(context, "[테스트] 이메일 인증 성공", Toast.LENGTH_SHORT).show()
                                            return@AppButton
                                        }

                                        viewModel.verifyEmailCode(email, emailCode) { ok ->
                                            if (ok) {
                                                isEmailVerified = true
                                                isEmailSent = false
                                                Toast.makeText(context, "인증 성공", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "인증 실패", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                } // <--- 이메일 입력 섹션 (hasEmail == false)의 닫는 괄호

                // 전화번호
                if (hasPhone) {
                    AppInputField(
                        value = phone,
                        onValueChange = {}, // 이미 등록된 번호는 수정 불가
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
                // 보호자 이메일 (항상 수정 가능, 중복 체크 안 함)
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
                        label = "보호자 이름",
                        outlined = true,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. 보호자 이메일 입력 및 전송
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
                                enabled = !isProtEmailVerified,
                                onClick = {
                                    if (protEmail.isNotBlank() && protName.isNotBlank()) {

                                        // 테스트 코드
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

                                        // [수정] 실제코드: 여기서는 ViewModel 함수만 호출
                                        // 결과(성공/실패)는 상단의 LaunchedEffect에서 처리
                                        viewModel.sendEmailCode(protEmail, protName)

                                    } else {
                                        Toast.makeText(context, "보호자 이름과 이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    )
                }

                // 보호자 이메일 인증번호 입력
                if (isProtEmailSent && !isProtEmailVerified) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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

                                        // 테스트 코드
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

                                        viewModel.verifyEmailCode(protEmail, protEmailCode) { ok ->
                                            if (ok) {
                                                isProtEmailVerified = true
                                                isProtEmailSent = false
                                                Toast.makeText(context, "인증 성공", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "인증 실패", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                // 저장 버튼
                AppButton(
                    text = editDone,
                    onClick = {
                        // 소셜 로그인 필수 정보 체크
                        if (isSocialLogin) {
                            if (name.isBlank()) {
                                Toast.makeText(context, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                                return@AppButton
                            }
                            if (phone.isBlank()) {
                                Toast.makeText(context, "전화번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                                return@AppButton
                            }
                            if (gender.isBlank()) {
                                Toast.makeText(context, "성별을 선택해주세요", Toast.LENGTH_SHORT).show()
                                return@AppButton
                            }
                            if (email.isBlank()) {
                                Toast.makeText(context, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                                return@AppButton
                            }
                            if (!isEmailVerified) {
                                Toast.makeText(context, "이메일 인증이 필요합니다", Toast.LENGTH_SHORT).show()
                                return@AppButton
                            }
                        }

                        if (protEmail.isNotBlank() && !isProtEmailVerified) {
                            Toast.makeText(context, "보호자 이메일 인증 필요", Toast.LENGTH_SHORT).show()
                            return@AppButton
                        }

                        // 생년월일 합치기
                        val birthDate =
                            if (birthYear.length == 4 && birthMonth.isNotBlank() && birthDay.isNotBlank()) {
                                val month = birthMonth.padStart(2, '0')
                                val day = birthDay.padStart(2, '0')
                                "$birthYear-$month-$day"
                            } else {
                                ""
                            }

                        viewModel.saveProfile(
                            username = name,
                            heightText = height,
                            weightText = weight,
                            ageText = birthDate,
                            email = email,
                            phone = phone,
                            prot_email = protEmail,
                            prot_name = protName, // [추가] 보호자 이름도 저장
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
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }
    }
}
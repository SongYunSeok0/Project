package com.mypage.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shared.R
import com.mypage.viewmodel.EditProfileEvent
import com.mypage.viewmodel.EditProfileViewModel
import com.shared.ui.components.AuthGenderDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel()
) {

    val profile by viewModel.profile.collectAsState()

    // 1125 로컬/소셜 구분 (이메일 유무 기준)
    val isLocal = !profile?.email.isNullOrEmpty()

    // --- 서버값 초기화 ---
    var name by remember(profile) { mutableStateOf(profile?.username ?: "") }
    var height by remember(profile) { mutableStateOf(profile?.height?.toString() ?: "") }
    var weight by remember(profile) { mutableStateOf(profile?.weight?.toString() ?: "") }
    var birthDate by rememberSaveable(profile) { mutableStateOf(profile?.birth_date ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var protEmail by remember(profile) { mutableStateOf(profile?.prot_email ?: "") }
    var gender by remember(profile) { mutableStateOf(profile?.gender ?: "") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "") }

    // 🔥 값 존재 여부에 따라 1회 입력/수정불가 결정
    val hasName = name.isNotBlank()
    val hasBirth = birthDate.isNotBlank()
    val hasGender = gender.isNotBlank()

    // 문자열 리소스화
    val editprofilephoto = stringResource(R.string.editprofilephoto)
    val editText = stringResource(R.string.edit)
    val emailText = stringResource(R.string.email)
    val nameText = stringResource(R.string.name)
    val heightText = stringResource(R.string.height)
    val weightText = stringResource(R.string.weight)
    val birthText = stringResource(R.string.birth)      // 🔥 "생년월일"
    val genderText = stringResource(R.string.gender)
    val phoneNumberPlaceholderText = stringResource(R.string.phone_number_placeholder)
    val editDone = stringResource(R.string.edit_done)

    val context = LocalContext.current

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

        // TODO: 프로필 사진 영역 (기존 코드 유지)
        // Text(editprofilephoto) 등…

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // ============================
            // 🔵 이름(username)
            // - 값 있으면 수정불가
            // - 값 없으면 1회 입력 가능
            // ============================
            if (hasName) {
                ReadonlyField(nameText, name)
            } else {
                EditableField(nameText, name) { name = it }
            }

            // 🔵 키/몸무게 (항상 수정 가능)
            EditableField(heightText, height) { height = it }
            EditableField(weightText, weight) { weight = it }

            /// ============================
// 🔵 생년월일(birth_date)
// - 정상 날짜("yyyy-mm-dd")면 Readonly
// - 그 외는 무조건 Editable
// ============================

            // 🔥 yyyy-mm-dd 날짜 형식 검증 함수
            fun isValidBirthFormat(value: String): Boolean {
                return Regex("""^\d{4}-\d{2}-\d{2}$""").matches(value)
            }

// 🔥 정상 날짜 형식일 때만 필드 닫기
            val hasValidBirth = isValidBirthFormat(birthDate)

            if (hasValidBirth) {
                // 값 있고 형식까지 맞으면 → Readonly
                ReadonlyField(birthText, birthDate)
            } else {
                // 값이 없거나, 입력 중(2,20,200 등), 형식 미완성 → Editable
                EditableField(
                    label = "${birthText} (예: 2000-10-10)",
                    value = birthDate,
                    onValueChange = { input ->
                        birthDate = input   // 🔥 그대로 저장 → 절대 자동 닫힘 없음
                    }
                )
            }



            // ============================
            // 🔵 성별(gender)
            // - 값 있으면 수정불가
            // - 값 없으면 드롭다운으로 선택 1회
            // ============================
            if (hasGender) {
                ReadonlyField(genderText, gender)
            } else {
                AuthGenderDropdown(
                    value = gender,
                    onValueChange = { gender = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ============================
            // 🔵 이메일(email)
            // - 로컬유저만 표시, 항상 수정불가
            // - 소셜유저는 이메일 필드 자체를 숨김
            // ============================
            if (isLocal) {
                ReadonlyField(emailText, email)
            }

            // 🔵 전화번호 / 보호자 이메일 (항상 수정 가능)
            EditableField(phoneNumberPlaceholderText, phone) { phone = it }
            EditableField("보호자 이메일", protEmail) { protEmail = it }
        }

        Spacer(Modifier.height(16.dp))

        // 🔹 저장 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable {
                    // 🔥 생년월일은 한 필드 그대로 전달 (이름처럼)
                    viewModel.saveProfile(
                        username = name,
                        heightText = height,
                        weightText = weight,
                        ageText = birthDate,   // <- 백엔드에서 birth_date 로 매핑되는 기존 파라미터 이름 유지
                        email = email,
                        phone = phone,
                        prot_email = protEmail,
                        gender = gender,
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

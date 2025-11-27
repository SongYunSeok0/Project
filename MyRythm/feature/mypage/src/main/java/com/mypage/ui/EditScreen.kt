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
import com.mypage.viewmodel.MyPageViewModel

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
    //var email by remember(profile) { mutableStateOf(profile?.email ?: "") }

    // 🔥 값 존재 여부에 따라 1회 입력/수정불가 결정
    val hasName = name.isNotBlank()
    val hasBirth = birthDate.isNotBlank()
    val hasGender = gender.isNotBlank()

    //1124 수정
    var email by remember { mutableStateOf("") }
    LaunchedEffect(profile) {
        profile?.let {
            email = it.email ?: ""
        }
    }
    // 문자열 리소스화
    val editprofilephoto = stringResource(R.string.editprofilephoto)
    val editText = stringResource(R.string.edit)
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
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // 1125 유저네임 username 데이터값 없을 경우 입력 가능(입력 후 수정 불가)
            if (hasName) {
                ReadonlyField(nameText, name)
            } else {
                EditableField(nameText, name) { name = it }
            }
            EditableField(heightText, height) { height = it }
            EditableField(weightText, weight) { weight = it }
            // 1125 생년월일 birthDate 데이터값 없을 경우 입력 가능(입력 후 수정 불가)
            // yyyy-mm-dd 형식이 아닐 경우 저장되지 않음 ex) 2000만 입력 시 저장x 2000-10-10 입력 시 데이터베이스 유저생년월일로 저장o&수정불가
            fun isValidBirthFormat(value: String): Boolean {
                return Regex("""^\d{4}-\d{2}-\d{2}$""").matches(value)
            }
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
            // 1125 성별 gender 데이터값 없을 경우 드롭다운으로 선택 가능(입력 후 수정 불가)
            if (hasGender) {
                ReadonlyField(genderText, gender)
            } else {
                AuthGenderDropdown(
                    value = gender,
                    onValueChange = { gender = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // 1125 이메일 email 로컬유저는 수정 불가, 소셜로그인 사용자는 필드x
            if (isLocal) {
                ReadonlyField(emailText, email)
            }
            EditableField(phoneNumberPlaceholderText, phone) { phone = it }
            EditableField(guardianEmailText, protEmail) { protEmail = it }
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
                    // 1125 생년월일은 한 필드 그대로 전달 (signup의 yyyy/mm/dd 나눠서 조립 안하고 yyyy-mm-dd 자체로 전달)
                    viewModel.saveProfile(
                        username = name,
                        heightText = height,
                        weightText = weight,
                        ageText = birthDate,   // 1125 백엔드에서 birth_date 로 매핑되는 기존 파라미터 이름 유지
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

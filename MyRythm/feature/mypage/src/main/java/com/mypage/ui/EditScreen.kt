package com.mypage.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.common.design.R
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

    // ⚡ 서버에서 받은 값으로 초기값 설정 · 서버에서 값 오기 전 null이면 "" 처리
    var name by remember(profile) { mutableStateOf(profile?.username ?: "") }
    var height by remember(profile) { mutableStateOf(profile?.height?.toString() ?: "") }
    var weight by remember(profile) { mutableStateOf(profile?.weight?.toString() ?: "") }
    var age by remember(profile) { mutableStateOf(profile?.age?.toString() ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone?.toString() ?: "") }
    var prot_phone by remember(profile) { mutableStateOf(profile?.prot_phone?.toString() ?: "") }
    var email by remember(profile) { mutableStateOf(profile?.email?.toString() ?: "") }

    //문자열 리소스화
    val editprofilephoto = stringResource(R.string.editprofilephoto)
    val editText = stringResource(R.string.edit)
    val nameText = stringResource(R.string.name)
    val heightText = stringResource(R.string.height)
    val weightText = stringResource(R.string.weight)
    val ageText = stringResource(R.string.age)
    val phoneNumberPlaceholderText = stringResource(R.string.phone_number_placeholder)
    val genderText = stringResource(R.string.gender)
    val bloodTypeText = stringResource(R.string.bloodtype)
    val editDone = stringResource(R.string.edit_done)

    val context = LocalContext.current

    // 저장 이벤트 처리
    LaunchedEffect(Unit) {
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
        /* ... 중략: 프로필 사진 UI 동일 ... */

        // 🔹 입력 필드
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EditableField(nameText, name) { name = it }
            EditableField(heightText, height) { height = it }
            EditableField(weightText, weight) { weight = it }
            EditableField(ageText, age) { age = it }
            EditableField("이메일", email) { email = it }
            EditableField(phoneNumberPlaceholderText, phone) { phone = it }
            EditableField("보호자 휴대폰 번호(01012345678)", prot_phone) { prot_phone = it }


        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 저장 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable {
                    viewModel.saveProfile(
                        username = name,
                        heightText = height,
                        weightText = weight,
                        ageText = age,
                        email = email,
                        phone = phone,
                        prot_phone = prot_phone,
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
fun SelectableButtonGroup(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, fontSize = 14.sp, color = Color(0xff3b566e))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (option == selectedOption) MaterialTheme.colorScheme.primary
                            else Color(0xffdddddd)
                        )
                        .clickable { onOptionSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (option == selectedOption) MaterialTheme.colorScheme.surface
                        else Color(0xff3b566e).copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

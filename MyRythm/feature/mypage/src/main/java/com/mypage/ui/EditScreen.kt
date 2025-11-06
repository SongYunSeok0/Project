package com.mypage.ui

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.common.design.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(userId: String? = null, modifier: Modifier = Modifier, onDone: () -> Unit = {}) {
    var name by remember { mutableStateOf("김이름") }
    var height by remember { mutableStateOf("170") }
    var weight by remember { mutableStateOf("47") }
    var age by remember { mutableStateOf("25") }
    var selectedGender by remember { mutableStateOf("남성") }
    var selectedBloodType by remember { mutableStateOf("A형") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 🔹 프로필 영역
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xffffb7c5))
                    .shadow(4.dp, RoundedCornerShape(999.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "😊", fontSize = 48.sp)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = -9.dp, y = -9.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xff6ae0d9))
                        .padding(5.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "프로필 사진 변경",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = name, fontSize = 16.sp, color = Color(0xff221f1f))
                Text(
                    text = "프로필 사진 변경",
                    fontSize = 14.sp,
                    color = Color(0xff5db0a8)
                )
            }

            Spacer(modifier = Modifier.width(66.dp))

            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xff6ae0d9))
                    .clickable { /* 편집 모드 로직 */ },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.edit),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = "편집", color = Color.White, fontSize = 16.sp)
                }
            }
        }

        // 🔹 입력 필드
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EditableField(label = "이름", value = name, onValueChange = { name = it })
            EditableField(label = "키 (cm)", value = height, onValueChange = { height = it })
            EditableField(label = "몸무게 (kg)", value = weight, onValueChange = { weight = it })
            EditableField(label = "나이", value = age, onValueChange = { age = it })

            // ✅ 성별 선택
            SelectableButtonGroup(
                label = "성별",
                options = listOf("남성", "여성"),
                selectedOption = selectedGender,
                onOptionSelected = { selectedGender = it }
            )

            // ✅ 혈액형 선택
            SelectableButtonGroup(
                label = "혈액형",
                options = listOf("A형", "B형", "AB형", "O형"),
                selectedOption = selectedBloodType,
                onOptionSelected = { selectedBloodType = it }
            )
        }

        // 🔹 수정 완료 버튼
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xff6ae0d9))
                .clickable { onDone() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.save),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "수정 완료", color = Color.White, fontSize = 16.sp)
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
                            if (option == selectedOption) Color(0xff6ae0d9)
                            else Color(0xffdddddd)
                        )
                        .clickable { onOptionSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (option == selectedOption) Color.White
                        else Color(0xff3b566e).copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

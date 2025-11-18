package com.mypage.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mypage.viewmodel.MyPageViewModel
import com.ui.components.AppSelectableButton

@Composable
fun InquiryTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "문의 유형",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("일반 문의", "버그 신고").forEach { type ->
                val isSelected = selectedType == type

                AppSelectableButton(
                    text = type,
                    selected = isSelected,
                    onClick = { onTypeSelected(type) },
                    modifier = Modifier.weight(1f),
                    height = 50.dp,
                    // 디자인용 색상은 이미 AppSelectableButton 내부에서 지정해놨음
                    useClickEffect = true
                )
            }
        }
    }
}

@Composable
fun NewInquiryForm(
    viewModel: MyPageViewModel = hiltViewModel()
) {
    var selectedType by remember { mutableStateOf("일반 문의") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    val context = LocalContext.current

    // 🔥 ViewModel 이벤트 수신
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MyPageEvent.InquirySubmitSuccess -> {
                    Toast.makeText(context, "문의가 등록되었습니다!", Toast.LENGTH_SHORT).show()
                }

                is MyPageEvent.InquirySubmitFailed -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                else -> Unit
            }
        }
    }

    Column {
        InquiryTypeSelector(
            selectedType = selectedType,
            onTypeSelected = { selectedType = it }
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {

            Text(
                text = "제목",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            InquiryTextField(
                value = title,
                onValueChange = { title = it },
                label = "문의 제목을 입력해주세요",
                singleLine = true,
                maxLines = 1
            )

            Text(
                text = "내용",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            InquiryTextField(
                value = content,
                onValueChange = { content = it },
                label = "문의 내용을 작성해주세요",
                height = 150.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            SubmitButton {
                if (title.isNotBlank() && content.isNotBlank()) {
                    viewModel.addInquiry(
                        type = selectedType,
                        title = title,
                        content = content
                    )

                    // 입력 초기화
                    title = ""
                    content = ""
                }
            }
        }
    }
}


@Composable
fun SubmitButton(
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF6AE0D9))
            .clickable { onClick() }
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "문의하기",
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 1.5.em
        )
    }
}

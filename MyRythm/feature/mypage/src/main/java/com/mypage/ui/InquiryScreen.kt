package com.mypage.ui

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ui.components.AppSelectableButton

// 1번탭 - 1:1 문의하기 - 1:1 문의 작성 화면

/*
1117 프리뷰용+중복로직이라고 해서 일단 주석처리
@Composable
fun InquiryScreen() {
    // 상위에서 상태 관리
    val inquiryList = remember { mutableStateListOf<Inquiry>() }

    AppTheme {
        Column(
            modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background)
        ) {
            // NewInquiryForm
            NewInquiryForm { type, title, content ->
                // 제출 시 리스트에 추가
                inquiryList.add(
                    Inquiry(
                        type = type,
                        title = title,
                        content = content
                    )
                )
            }

            Spacer(Modifier.height(24.dp))

            // InquiryHistory
            InquiryHistoryList(inquiries = inquiryList)
        }
    }
}

@Composable
fun InquiryHistoryList(inquiries: List<com.domain.model.Inquiry>) {
    Column {
        inquiries.forEach { inquiry ->
            //FAQItem(inquiry = inquiry)   // ✅ 통째로 전달
            InquiryCard(inquiry = inquiry)
        }
    }
}
*/
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
fun NewInquiryForm(onSubmit: (type: String, title: String, content: String) -> Unit) {
    var selectedType by remember { mutableStateOf("일반 문의") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    //var images by remember { mutableStateOf(listOf<Uri>()) }

    Column {
        // 문의 유형 선택
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
                style = MaterialTheme.typography.titleMedium, // AppTheme 기반 폰트 적용
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )
            InquiryTextField(
                value = title,
                onValueChange = { title = it },
                label = "문의 제목을 입력해주세요",
                singleLine = true,
                maxLines = 1
            )

            // 내용 입력 (3번 InquiryTextField 컴포넌트)
            Text(
                text = "내용",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )
            InquiryTextField(
                value = content,
                onValueChange = { content = it },
                label = "문의 내용을 작성해주세요",
                height = 150.dp
            )

            /*// 이미지 첨부 (3번 ImageAttachmentSection 컴포넌트)
        ImageAttachmentSection(
            images = images,
            onImagesSelected = { newImages ->
                images = newImages
            },
            onImageRemove = { index ->
                images = images.filterIndexed { i, _ -> i != index }
            }
        )
*/

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(height = 91.dp)
            ) {
                Column {
                    Text(
                        text = "• 영업일 기준 1-2일 이내에 답변드립니다.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 1.63.em,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(10.dp)
                    )
                    Text(
                        text = "• 개인정보가 포함된 문의는 1:1 문의를 이용해주세요.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 1.63.em,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(8.dp)
                            .requiredWidth(width = 310.dp)
                    )
                }
            }
            SubmitButton {
                if (title.isNotBlank() && content.isNotBlank()) {
                    onSubmit(selectedType, title, content)
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

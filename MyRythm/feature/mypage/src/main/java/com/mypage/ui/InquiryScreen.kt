package com.mypage.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.common.design.R
import com.domain.model.Inquiry

// 문의 데이터 클래스


@Composable
fun InquiryScreen() {
    // 상위에서 상태 관리
    val inquiryList = remember { mutableStateListOf<Inquiry>() }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        // NewInquiryForm
        NewInquiryForm { type, title, content ->
            // 제출 시 리스트에 추가
            inquiryList.add(Inquiry(
                type = type,
                title = title,
                content = content
            ))
        }

        Spacer(Modifier.height(24.dp))

        // InquiryHistory
        InquiryHistory(inquiries = inquiryList)


    }
}

@Composable
fun NewInquiryForm(onSubmit: (type: String, title: String, content: String) -> Unit) {
    var selectedType by remember { mutableStateOf("일반 문의") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Column {
        // 문의 유형 선택
        InquiryTypeSelector(
            selectedType = selectedType,
            onTypeSelected = { selectedType = it }
        )

        Spacer(Modifier.height(12.dp))

        // 제목 입력
        Column {
            // 제목
            Text(
                text = "제목",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF101828),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("문의 제목을 입력해주세요") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // 내용
            Text(
                text = "내용",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF101828),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("문의 내용을 작성해주세요") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }


        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(height = 91.dp)
        ) {
            Column{
                Text(
                    text = "• 영업일 기준 1-2일 이내에 답변드립니다.",
                    color = Color(0xff5db0a8),
                    lineHeight = 1.63.em,
                    style = TextStyle(
                        fontSize = 14.sp
                    ),
                    modifier = Modifier
                        .padding(10.dp)
                )
                Text(
                    text = "• 개인정보가 포함된 문의는 1:1 문의를 이용해주세요.",
                    color = Color(0xff5db0a8),
                    lineHeight = 1.63.em,
                    style = TextStyle(
                        fontSize = 14.sp
                    ),
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



@Composable
fun InquiryHistory(inquiries: List<com.domain.model.Inquiry>) {
    Column {
        inquiries.forEach { inquiry ->
            FAQItem(inquiry = inquiry)   // ✅ 통째로 전달
        }
    }
}



@Composable
fun FAQItem(inquiry: com.domain.model.Inquiry) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {

        Card(
            colors = CardDefaults.cardColors(Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { expanded = !expanded }
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.chat),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "[${inquiry.type}] ${inquiry.title}",
                        fontSize = 16.sp,
                        color = Color(0xFF101828),
                        lineHeight = 1.5.em
                    )
                }

                Image(
                    painter = painterResource(
                        id = if (expanded) R.drawable.arrow_up else R.drawable.arrow_down
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (expanded) {
            Card(
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Text(
                        text = "문의 내용",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF101828)
                    )
                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = inquiry.content,
                        fontSize = 14.sp,
                        color = Color(0xFF6A7282),
                        lineHeight = 1.5.em
                    )

                    if (!inquiry.answer.isNullOrBlank()) {
                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "관리자 답변",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6AE0D9)
                        )
                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = inquiry.answer!!,
                            fontSize = 14.sp,
                            color = Color(0xFF6A7282),
                            lineHeight = 1.5.em
                        )
                    } else {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "아직 답변이 등록되지 않았습니다.",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF),
                            lineHeight = 1.5.em
                        )
                    }
                }
            }
        }
    }
}





@Composable
fun InquiryTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()

    ) {
        Text(
            text = "문의 유형",
            color = Color(0xFF101828),
            fontSize = 20.sp,
            lineHeight = 1.5.em,
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("일반 문의", "버그 신고").forEach { type ->
                val isSelected = selectedType == type
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) Color(0xFF6AE0D9).copy(alpha = 0.1f) else Color.White,
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = if (isSelected) Color(0xFF6AE0D9) else Color(0xFFE5E7EB)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTypeSelected(type) }
                        .height(75.dp)

                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = type,
                            color = if (isSelected) Color(0xFF6AE0D9) else Color(0xFF6A7282),
                            fontSize = 14.sp,
                            lineHeight = 1.43.em
                        )
                    }
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




@Preview(widthDp = 412, heightDp = 917)
@Composable
private fun InquiryPreview() {
    InquiryScreen()
}

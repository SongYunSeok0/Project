package com.mypage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domain.model.Inquiry
import com.mypage.viewmodel.MyPageViewModel

@Composable
fun FAQScreen(inquiries: List<Inquiry>,
              onSubmit: (type: String, title: String, content: String) -> Unit,
              modifier: Modifier = Modifier) {

    var selectedTab by remember { mutableStateOf("history") }


    Column(modifier = modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
            TabHeader("나의 문의 내역", selectedTab == "history") { selectedTab = "history" }
            TabHeader("문의 작성", selectedTab == "new") { selectedTab = "new" }
        }

        Spacer(Modifier.height(24.dp))

        when(selectedTab) {
            "history" -> InquiryHistory(inquiries)   // ✅ 리스트 전달
            "new" -> NewInquiryForm(onSubmit)
        }
    }
}


@Composable
fun RowScope.TabHeader(title: String, selected: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color(0xFF6AE0D9) else Color(0xFF666666),
            textAlign = TextAlign.Center,
            lineHeight = 1.5.em,
            modifier = Modifier
                .clickable { onClick() }
                .padding(bottom = 13.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(if (selected) Color(0xFF6AE0D9) else Color(0xFFE0E0E0))
        )
    }
}

@Composable
fun FAQScreenWrapper(
    viewModel: MyPageViewModel = hiltViewModel()
) {
    val inquiries by viewModel.inquiries.collectAsState()

    FAQScreen(
        inquiries = inquiries.map {
            com.domain.model.Inquiry(
                type = it.type,
                title = it.title,
                content = it.content,
                answer = it.answer
            )
        },
        onSubmit = { type, title, content ->
            viewModel.addInquiry(type, title, content)
        }
    )
}







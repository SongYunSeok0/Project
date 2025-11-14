package com.mypage.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.common.design.R

// FAQScreen.kt 에서 사용되는 문의 내역+답변 카드 컴포넌트
// 해당 컴포넌트는 디자인 용도, Inquiry 모델은 도메인레이어로 분리

/*
:domain 모듈의 데이터클래스

data class Inquiry(
    val id: Int = 0,
    val type: String,
    val title: String,
    val content: String,
    val answer: String? = null
)
 */

// enum 클래스에 답변 블록 표시 여부 속성 추가
enum class InquiryStatus(val text: String, val color: Color, val showAnswerBlock: Boolean) {
    UNANSWERED("미답변", Color(0xFFABABAB), false),
    ANSWERED("답변완료", Color(0xFF5DB0A8), true)
}

@Composable
fun InquiryCard(
    inquiry: Inquiry,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val upIcon = R.drawable.up_chevron
    val downIcon = R.drawable.down_chevron
    val chatIcon = R.drawable.faqchat

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(
                border = BorderStroke(0.7.dp, Color.Gray),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        // 카드 상단 Row: 아이콘 + 제목 / 상태
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painterResource(chatIcon),
                contentDescription = "FAQ 아이콘",
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(10.dp)) // 아이콘과 제목 사이 간격

            // 제목 + 답변/미답변 + 업/다운 아이콘
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = inquiry.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 30.dp) // 제목의 글자수 길어지면 ... 처리용 간격
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 답변/미답변
                    Text(
                        text = inquiry.status.text,
                        color = inquiry.status.color,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // 업/다운 아이콘
                    Image(
                        painter = painterResource(if (expanded) upIcon else downIcon),
                        contentDescription = if (expanded) "닫기" else "열기",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 확장된 내용 (마크다운 형태로 표시 예정)
        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))

            // 질문 블록 (항상 표시)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE4F5F4))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = inquiry.title,
                        color = Color(0xFF5DB0A8),
                        fontSize = 14.sp,
                        lineHeight = 1.63.em
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = inquiry.questionDate,
                        color = Color(0xFF5DB0A8),
                        fontSize = 14.sp,
                        lineHeight = 1.63.em
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "문의 내용: ${inquiry.content}",
                    color = Color(0xFF5DB0A8),
                    fontSize = 14.sp,
                    lineHeight = 1.63.em
                )
            }

            // ✅ 답변 블록 표시 여부는 enum 속성에서 결정
            if (inquiry.status.showAnswerBlock) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFDFFDFB))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "답변:",
                            color = Color(0xFF5DB0A8),
                            fontSize = 14.sp,
                            lineHeight = 1.63.em
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = inquiry.answerDate,
                            color = Color(0xFF5DB0A8),
                            fontSize = 14.sp,
                            lineHeight = 1.63.em
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = inquiry.answer,
                        color = Color(0xFF5DB0A8),
                        fontSize = 14.sp,
                        lineHeight = 1.63.em
                    )
                }
            }
        }
    }
}
// 파일 끝에 추가
@Preview(showBackground = true)
@Composable
private fun InquiryCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InquiryCard(
                inquiry = Inquiry(
                    id = 1,
                    title = "테스트 문의",
                    status = InquiryStatus.UNANSWERED,
                    content = "문의 내용입니다"
                )
            )

            InquiryCard(
                inquiry = Inquiry(
                    id = 2,
                    title = "답변 완료된 문의",
                    status = InquiryStatus.ANSWERED,
                    content = "문의 내용입니다",
                    answer = "답변 내용입니다",
                    answerDate = "2025/11/05"
                )
            )
        }
    }
}

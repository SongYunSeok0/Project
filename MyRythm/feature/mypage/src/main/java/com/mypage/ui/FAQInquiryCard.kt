package com.mypage.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shared.R
import com.domain.model.Inquiry
import com.shared.ui.theme.AppTheme
import com.shared.ui.theme.componentTheme

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
enum class InquiryStatus(val text: String, val showAnswerBlock: Boolean) {
    UNANSWERED("미답변", false),
    ANSWERED("답변완료", true)
}

// enum 내부의 문자열은 건드리지 말고 문자열 리소스 매핑, 이후 ui엔 status.toDisplayText() 사용
@Composable
fun InquiryStatus.toDisplayText(): String {
    return when (this) {
        InquiryStatus.UNANSWERED -> stringResource(R.string.inquiry_status_unanswered)
        InquiryStatus.ANSWERED -> stringResource(R.string.inquiry_status_answered)
    }
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

    // answer 필드로 상태 판단
    val status = if (inquiry.answer.isNullOrBlank()) {
        InquiryStatus.UNANSWERED
    } else {
        InquiryStatus.ANSWERED
    }

    val statusColor = when (status) {
        InquiryStatus.UNANSWERED -> Color.Gray
        InquiryStatus.ANSWERED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    // 날짜는 기본값 (추후 domain에 필드 추가 가능, 현재는 임의 설정)
    val questionDate = "2025/11/03"
    val answerDate = "2025/11/04"

    val faqIcon = stringResource(R.string.faqicon)
    val expandText = stringResource(R.string.expand)
    val collapseText = stringResource(R.string.collapse)
    val answerText = stringResource(R.string.answer)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(
                border = BorderStroke(0.7.dp, Color.Gray),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { expanded = !expanded }
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
                contentDescription = faqIcon,
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
                    maxLines = 2,
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
                        text = status.toDisplayText(),
                        color = when (status) {
                            InquiryStatus.UNANSWERED -> Color.Gray
                            InquiryStatus.ANSWERED -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // 업/다운 아이콘
                    Image(
                        painter = painterResource(if (expanded) upIcon else downIcon),
                        contentDescription = if (expanded) collapseText else expandText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        // 확장된 내용 (마크다운 형태로 표시 예정)
        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))

            // 질문 블록 (항상 표시)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.componentTheme.inquiryCardQuestion)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "[${inquiry.type}]\n${inquiry.title}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = inquiry.content,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // 질문 블록 날짜 우측 상단 고정
                Text(
                    text = questionDate,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            // 답변 블록 표시 여부는 enum 속성에서 결정
            if (status.showAnswerBlock && !inquiry.answer.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.componentTheme.inquiryCardAnswer)
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = answerText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = inquiry.answer!!,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // 답변 블록 날짜 우측 상단 고정
                    Text(
                        text = answerDate,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InquiryCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InquiryCard(
                inquiry = Inquiry(
                    id = 1,
                    type = "일반 문의",
                    title = "테스트 문의",
                    content = "문의 내용입니다",
                    answer = null
                )
            )

            InquiryCard(
                inquiry = Inquiry(
                    id = 2,
                    type = "버그 신고",
                    title = "답변 완료된 문의",
                    content = "문의 내용입니다",
                    answer = "답변 내용입니다"
                )
            )
        }
    }
}
// mypage/ui/InquiryCard.kt
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
import com.domain.model.InquiryComment
import com.shared.ui.theme.AppTheme
import com.shared.ui.theme.componentTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class InquiryStatus(val text: String, val showAnswerBlock: Boolean) {
    UNANSWERED("미답변", false),
    ANSWERED("답변완료", true)
}

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

    // 🔥 isAnswered 필드로 상태 판단
    val status = if (inquiry.isAnswered) {
        InquiryStatus.ANSWERED
    } else {
        InquiryStatus.UNANSWERED
    }

    val statusColor = when (status) {
        InquiryStatus.UNANSWERED -> MaterialTheme.colorScheme.surfaceVariant
        InquiryStatus.ANSWERED -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // 🔥 날짜 포맷팅
    val questionDate = inquiry.createdAt?.let { formatDate(it) } ?: "-"
    val answerDate = inquiry.comments.firstOrNull()?.createdAt?.let { formatDate(it) } ?: "-"

    val faqIcon = stringResource(R.string.faqicon)
    val expandText = stringResource(R.string.expand)
    val collapseText = stringResource(R.string.collapse)
    val answerText = stringResource(R.string.answer)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = MaterialTheme.shapes.large
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { expanded = !expanded }
            .padding(16.dp)
    ) {
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

            Spacer(modifier = Modifier.width(10.dp))

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
                        .padding(end = 30.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = status.toDisplayText(),
                        color = when (status) {
                            InquiryStatus.UNANSWERED -> MaterialTheme.colorScheme.surfaceVariant
                            InquiryStatus.ANSWERED -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Image(
                        painter = painterResource(if (expanded) upIcon else downIcon),
                        contentDescription = if (expanded) collapseText else expandText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))

            // 질문 블록
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
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

                Text(
                    text = questionDate,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            // 🔥 답변 블록 (comments가 있을 때만 표시)
            if (status.showAnswerBlock && inquiry.comments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                // 🔥 첫 번째 댓글만 표시 (또는 모든 댓글 표시 가능)
                val firstAnswer = inquiry.comments.first()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
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
                            text = firstAnswer.content,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

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

// 🔥 날짜 포맷팅 함수
private fun formatDate(dateTimeString: String): String {
    return try {
        val instant = Instant.parse(dateTimeString)
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        instant.atZone(ZoneId.systemDefault()).format(formatter)
    } catch (e: Exception) {
        dateTimeString
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
            // 미답변
            InquiryCard(
                inquiry = Inquiry(
                    id = 1,
                    userId = 1,
                    username = "사용자1",
                    type = "일반 문의",
                    title = "테스트 문의",
                    content = "문의 내용입니다",
                    isAnswered = false,
                    createdAt = "2025-11-03T10:00:00Z",
                    commentCount = 0,
                    comments = emptyList()
                )
            )

            // 답변 완료
            InquiryCard(
                inquiry = Inquiry(
                    id = 2,
                    userId = 1,
                    username = "사용자2",
                    type = "버그 신고",
                    title = "답변 완료된 문의",
                    content = "문의 내용입니다",
                    isAnswered = true,
                    createdAt = "2025-11-03T10:00:00Z",
                    commentCount = 1,
                    comments = listOf(
                        InquiryComment(
                            id = 1,
                            inquiryId = 2,
                            userId = 999,
                            username = "관리자",
                            content = "답변 내용입니다",
                            createdAt = "2025-11-04T14:00:00Z",
                            isStaff = true
                        )
                    )
                )
            )
        }
    }
}
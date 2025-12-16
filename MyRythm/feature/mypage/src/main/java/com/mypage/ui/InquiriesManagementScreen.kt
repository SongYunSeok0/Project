package com.mypage.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domain.model.Inquiry
import com.domain.model.InquiryComment
import com.mypage.viewmodel.StaffManagementViewModel
import com.shared.R
import com.shared.bar.AppTopBar
import com.shared.ui.components.AppButton
import com.shared.ui.components.AppIconButton
import com.shared.ui.components.AppInputField
import com.shared.ui.theme.AppTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquiriesManagementScreen(
    viewModel: StaffManagementViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val inquiries by viewModel.filteredInquiries.collectAsState()
    val selectedInquiry by viewModel.selectedInquiry.collectAsState()
    val inquiryComments by viewModel.inquiryComments.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    // 🔙 하드웨어 뒤로가기
    BackHandler {
        if (selectedInquiry != null) {
            viewModel.backToInquiryList()
        } else {
            onBackClick()
        }
    }

    // 에러 토스트
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    // 댓글 작성 결과
    LaunchedEffect(Unit) {
        viewModel.commentAdded.collect { success ->
            val message = if (success) "답변이 등록되었습니다." else "답변 등록에 실패했습니다."
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 화면 진입 시 문의사항 목록 로드
    LaunchedEffect(Unit) {
        viewModel.loadInquiries()
    }

    AppTheme {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = if (selectedInquiry != null) "문의사항 상세" else "문의사항 관리",
                    showBack = true,
                    onBackClick = {
                        if (selectedInquiry != null) {
                            viewModel.backToInquiryList()
                        } else {
                            onBackClick()
                        }
                    },
                    showSearch = false
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    when (selectedInquiry) {
                        null -> {
                            // 문의사항 목록 화면
                            SearchBar(
                                query = searchQuery,
                                onQueryChange = { viewModel.updateSearchQuery(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )

                            InquiriesListContent(
                                inquiries = inquiries,
                                onInquiryClick = { viewModel.selectInquiry(it) }
                            )
                        }

                        else -> {
                            // 문의사항 상세 + 댓글 화면
                            InquiryDetailContent(
                                inquiry = selectedInquiry!!,
                                comments = inquiryComments,
                                onAddComment = { content ->
                                    viewModel.addComment(selectedInquiry!!.id, content)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// 🔥 검색창
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val faqSearchBarText = stringResource(R.string.faqSearchbar)
    val searchText = stringResource(R.string.search)
    val clearText = stringResource(R.string.clear)

    AppInputField(
        value = query,
        onValueChange = onQueryChange,
        label = faqSearchBarText,
        modifier = modifier,
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = searchText,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            if (query.isNotEmpty()) {
                AppIconButton(
                    onClick = { onQueryChange("") },
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    icon = {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = clearText
                        )
                    }
                )
            }
        }
    )
}

// 🔥 문의사항 목록
@Composable
private fun InquiriesListContent(
    inquiries: List<Inquiry>,
    onInquiryClick: (Inquiry) -> Unit
) {
    val noAnswerMessage = stringResource(R.string.mypage_message_no_answer)

    if (inquiries.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = noAnswerMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(inquiries) { inquiry ->
                InquiryCard(
                    inquiry = inquiry,
                    onClick = { onInquiryClick(inquiry) }
                )
            }
        }
    }
}

// 🔥 문의사항 카드
@Composable
private fun InquiryCard(
    inquiry: Inquiry,
    onClick: () -> Unit
) {
    val chatIcon = R.drawable.faqchat
    val faqIcon = stringResource(R.string.faqicon)
    val answeredText = stringResource(R.string.inquiry_status_answered)
    val unansweredText = stringResource(R.string.inquiry_status_unanswered)
    val anonymousText = stringResource(R.string.anonymous)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.large
            )
            .background(MaterialTheme.colorScheme.background),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (inquiry.isAnswered)
                MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(chatIcon),
                        contentDescription = faqIcon,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = getCategoryText(inquiry.type),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraSmall)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = if (inquiry.isAnswered) answeredText else unansweredText,
                    style = MaterialTheme.typography.bodyLarge,
                            color = if (inquiry.isAnswered)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = inquiry.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = inquiry.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = inquiry.username ?: anonymousText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${inquiry.commentCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// 🔥 문의사항 상세 + 댓글
@Composable
private fun InquiryDetailContent(
    inquiry: Inquiry,
    comments: List<InquiryComment>,
    onAddComment: (String) -> Unit
) {
    val chatIcon = R.drawable.faqchat
    val faqIcon = stringResource(R.string.faqicon)
    var commentText by remember { mutableStateOf("") }
    val answerText = stringResource(R.string.answer_label)
    val sendText = stringResource(R.string.send)
    val anonymousText = stringResource(R.string.anonymous)
    val noAnswerMessage = stringResource(R.string.mypage_message_no_answer)
    val answerInputMessage = stringResource(R.string.mypage_message_answer_input)
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.large
                        )
                        .background(MaterialTheme.colorScheme.background),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getCategoryText(inquiry.type),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )

                            Text(
                                text = inquiry.username ?: anonymousText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = inquiry.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = inquiry.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        inquiry.createdAt?.let {
                            Text(
                                text = formatDateTime(it),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = answerText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${comments.size}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (comments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = noAnswerMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                items(comments) { comment ->
                    CommentItem(comment = comment)
                }
            }
        }
        Card(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                AppInputField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = answerInputMessage,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    imeAction = ImeAction.Send,
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (commentText.isNotBlank()) {
                                onAddComment(commentText)
                                commentText = ""
                            }
                        }
                    ),
                    maxLines = 5,
                    leadingContent = {
                        Image(
                            painter = painterResource(chatIcon),
                            contentDescription = faqIcon,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    trailingContent = {
                        AppButton(
                            isCircle = true,
                            width = 44.dp,
                            height = 44.dp,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    onAddComment(commentText)
                                    commentText = ""
                                }
                            },
                            enabled = commentText.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = sendText,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                )
            }
        }
    }
}


// 🔥 댓글 아이템
@Composable
private fun CommentItem(
    comment: InquiryComment
) {
    val anonymousText = stringResource(R.string.anonymous)
    val adminText = stringResource(R.string.label_admin)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (comment.isStaff)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (comment.isStaff)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (comment.isStaff) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = comment.username ?: anonymousText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (comment.isStaff) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = adminText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                comment.createdAt?.let {
                    Text(
                        text = formatDateTime(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// 🔥 카테고리 텍스트 변환
@Composable
private fun getCategoryText(category: String): String {
    val generalText = stringResource(R.string.general_inquiry)
    val accountText = stringResource(R.string.account_inquiry)
    val medicationText = stringResource(R.string.medication_inquiry)
    val deviceText = stringResource(R.string.device_inquiry)
    val otherText = stringResource(R.string.other_inquiry)
    return when (category) {
        "general" -> generalText
        "account" -> accountText
        "medication" -> medicationText
        "device" -> deviceText
        "other" -> otherText
        else -> category
    }
}

// 🔥 날짜 포맷
private fun formatDateTime(dateTimeString: String): String {
    return try {
        val instant = Instant.parse(dateTimeString)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        instant.atZone(ZoneId.systemDefault()).format(formatter)
    } catch (e: Exception) {
        dateTimeString
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
fun InquiriesManagement_Interactive_Preview() {
    AppTheme {

        // 미답변 / 답변완료 둘 다 테스트 가능하도록 상태 제공
        var isAnswered by remember { mutableStateOf(false) }

        // inquiry 샘플
        val inquiry = Inquiry(
            id = 1L,
            userId = 1L,
            username = if (isAnswered) "홍길동" else "김철수",
            title = if (isAnswered) "기기 등록이 안돼요" else "앱이 자꾸 종료됩니다",
            content = if (isAnswered) "블루투스 연결 이후 진행이 안됩니다." else "어제부터 실행하면 바로 종료됩니다.",
            type = if (isAnswered) "device" else "general",
            isAnswered = isAnswered,
            createdAt = "2024-12-01T12:00:00Z",
            commentCount = 0
        )

        // 댓글 상태
        var comments by remember {
            mutableStateOf(
                if (isAnswered) {
                    listOf(
                        InquiryComment(
                            id = 1L,
                            inquiryId = inquiry.id,
                            userId = 999L,
                            username = "관리자",
                            content = "확인 후 조치 완료했습니다!",
                            createdAt = "2024-12-01T14:00:00Z",
                            isStaff = true
                        )
                    )
                } else emptyList()
            )
        }

        // UI 표시
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

            // 스위치(답변 완료/미답변 테스트)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("미답변 / 답변완료 테스트", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = isAnswered,
                    onCheckedChange = {
                        isAnswered = it
                        comments = if (it) {
                            listOf(
                                InquiryComment(
                                    id = 1L,
                                    inquiryId = inquiry.id,
                                    userId = 999L,
                                    username = "관리자",
                                    content = "확인 후 조치 완료했습니다!",
                                    createdAt = "2024-12-01T14:00:00Z",
                                    isStaff = true
                                )
                            )
                        } else emptyList()
                    }
                )
            }

            InquiryDetailContent(
                inquiry = inquiry.copy(
                    isAnswered = isAnswered,
                    commentCount = comments.size
                ),
                comments = comments,
                onAddComment = { text ->
                    val newComment = InquiryComment(
                        id = comments.size + 1L,
                        inquiryId = inquiry.id,
                        userId = 999L,
                        username = "관리자",
                        content = text,
                        createdAt = "2024-12-01T14:30:00Z",
                        isStaff = true
                    )
                    comments = comments + newComment
                }
            )
        }
    }
}

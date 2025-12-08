package com.mypage.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domain.model.Inquiry
import com.domain.model.InquiryComment
import com.mypage.viewmodel.StaffManagementViewModel
import com.shared.R
import com.shared.bar.AppTopBar
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
    if (inquiries.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "문의사항이 없습니다",
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
                    text = if (inquiry.isAnswered) "답변 완료" else "미답변",
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
                        text = inquiry.username ?: "익명",
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
    var commentText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
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
                    //elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                                text = inquiry.username ?: "익명",
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        inquiry.createdAt?.let {
                            Text(
                                text = formatDateTime(it),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        text = "답변",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${comments.size}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            text = "아직 답변이 없습니다",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.surfaceVariant
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
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                AppInputField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier.weight(1f),
                    label = "답변을 입력하세요",
                    //minLines = 2,
                    maxLines = 5,
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onAddComment(commentText)
                            commentText = ""
                        }
                    },
                    enabled = commentText.isNotBlank(),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Send, "전송")
                }
            }
        }
    }
}

// 🔥 댓글 아이템
@Composable
private fun CommentItem(
    comment: InquiryComment
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (comment.isStaff)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.primary
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
                        text = comment.username ?: "익명",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (comment.isStaff) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "관리자",
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
private fun getCategoryText(category: String): String {
    return when (category) {
        "general" -> "일반 문의"
        "account" -> "계정 관련"
        "medication" -> "복약 관련"
        "device" -> "기기 관련"
        "other" -> "기타"
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
fun InquiriesManagement_Preview() {
    AppTheme {

        // Preview 전용 상태
        var selectedInquiry by remember { mutableStateOf<Inquiry?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        var commentText by remember { mutableStateOf("") }

        // stringResource / painterResource 없이 더미 반환
        fun fakeString(s: String) = s

        // Preview-safe dummy image
        @Composable
        fun FakeIconBox() {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
        }

        // Dummy Inquiry list
        val inquiries = listOf(
            Inquiry(
                id = 1L,
                userId = 1L,
                username = "홍길동",
                title = "앱이 종료돼요",
                content = "어제부터 계속 튕깁니다.",
                type = "general",
                isAnswered = false,
                createdAt = "2024-12-01T12:00:00Z",
                commentCount = 1
            ),
            Inquiry(
                id = 2L,
                userId = 2L,
                username = "김철수",
                title = "비밀번호 재설정 불가",
                content = "메일이 오지 않습니다.",
                type = "account",
                isAnswered = true,
                createdAt = "2024-12-02T09:00:00Z",
                commentCount = 3
            )
        )

        // Dummy Comment list
        val comments = listOf(
            InquiryComment(
                id = 1L,
                inquiryId = 1L,
                userId = 999L,
                username = "관리자",
                content = "캐시 삭제 후 다시 실행해보세요!",
                createdAt = "2024-12-01T13:00:00Z",
                isStaff = true
            ),
            InquiryComment(
                id = 2L,
                inquiryId = 1L,
                userId = 1L,
                username = "홍길동",
                content = "해결됐습니다 감사합니다!",
                createdAt = "2024-12-01T13:30:00Z",
                isStaff = false
            )
        )

        Scaffold(
            topBar = {
                AppTopBar(
                    title = if (selectedInquiry != null) "문의사항 상세" else "문의사항 관리",
                    showBack = selectedInquiry != null,
                    onBackClick = { selectedInquiry = null },
                    showSearch = false
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {

                // 목록 화면
                if (selectedInquiry == null) {

                    // 검색창 (AppInputField 그대로 유지)
                    AppInputField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = fakeString("검색"),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        leadingContent = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            if (searchQuery.isNotEmpty()) {
                                AppIconButton(
                                    onClick = { searchQuery = "" },
                                    icon = { Icon(Icons.Default.Clear, "clear") }
                                )
                            }
                        }
                    )

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(inquiries) { item ->
                            // 🔥 실제 InquiryCard 디자인 그대로 사용
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.large)
                                    .clickable { selectedInquiry = item }
                                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.large)
                                    .background(MaterialTheme.colorScheme.surface),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (item.isAnswered)
                                        MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)
                                    else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {

                                            // preview-safe fake icon
                                            FakeIconBox()

                                            Spacer(Modifier.width(8.dp))

                                            Text(
                                                text = getCategoryText(item.type),
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier
                                                    .clip(MaterialTheme.shapes.extraSmall)
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = if (item.isAnswered) "답변 완료" else "미답변",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = if (item.isAnswered)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.error
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    Text(
                                        text = item.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(item.username ?: "익명")
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.ChatBubbleOutline, null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("${item.commentCount}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 상세 화면
                else {
                    InquiryDetailContent(
                        inquiry = selectedInquiry!!,
                        comments = comments,
                        onAddComment = { /* preview no-op */ }
                    )
                }
            }
        }
    }
}

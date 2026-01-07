package com.mypage.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shared.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.shared.bar.AppTopBar
import com.domain.model.Inquiry
import com.domain.model.InquiryComment
import com.mypage.viewmodel.InquiryViewModel
import com.shared.ui.theme.AppTheme
import com.shared.ui.theme.OnlyColorTheme
import kotlinx.coroutines.launch

@Composable
fun FAQScreen(
    onSubmit: (type: String, title: String, content: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val faqText = stringResource(R.string.faq)

    val pagerState = rememberPagerState(initialPage = 0) { 2 }

    AppTheme {
        Column(modifier = modifier
            .fillMaxSize()
        ) {
            FAQTabRow(pagerState = pagerState)

            FAQTabContent(
                pagerState = pagerState,
                onSubmit = onSubmit
            )
        }
    }
}

@Composable
fun FAQScreenWrapper(
    viewModel: InquiryViewModel = hiltViewModel(),
) {
    FAQScreen(
        onSubmit = { type, title, content ->
            viewModel.addInquiry(type, title, content)
        }
    )
}

@Composable
private fun InquiryHistory(
    inquiries: List<Inquiry>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(inquiries) { inquiry ->
            InquiryCard(
                inquiry = inquiry
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FAQTabRow(pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    val tabs = listOf(
        stringResource(id = R.string.myinquirylist),
        stringResource(id = R.string.one_on_one_inquiry)
    )

    PrimaryTabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier
                    .tabIndicatorOffset(pagerState.currentPage),
                color = MaterialTheme.colorScheme.primary,
                height = 2.dp
            )
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.height(50.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun FAQTabContent(
    pagerState: PagerState,
    onSubmit: (type: String, title: String, content: String) -> Unit,
    viewModel: InquiryViewModel = hiltViewModel()
) {
    val inquiries by viewModel.inquiries.collectAsState()

    HorizontalPager(state = pagerState) { index ->
        when (index) {
            0 -> InquiryHistory(inquiries = inquiries)
            1 -> NewInquiryForm()
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
fun FAQScreenWithSampleDataPreview() {
    // 🔥 샘플 데이터 - 새로운 구조에 맞춰 수정
    val sampleInquiries = listOf(
        // 🔴 1. 미답변 (가장 상위)
        Inquiry(
            id = 1,
            userId = 100,
            username = "사용자1",
            type = "일반문의",
            title = "등록방법",
            content = "회원 가입 후 약 정보는 어디에서 등록하나요?",
            isAnswered = false,
            createdAt = "2025-11-08T09:00:00Z",
            commentCount = 0,
            comments = emptyList()
        ),

        // 🟢 2. 답변완료
        Inquiry(
            id = 2,
            userId = 100,
            username = "사용자1",
            type = "일반문의",
            title = "약 복용 알림은 어떻게 설정하나요?",
            content = "복용 시간 알림 설정 방법이 궁금합니다.",
            isAnswered = true,
            createdAt = "2025-11-07T10:30:00Z",
            commentCount = 1,
            comments = listOf(
                InquiryComment(
                    id = 1,
                    inquiryId = 2,
                    userId = 999,
                    username = "관리자",
                    content = "스케줄러 메뉴에서 복용 시간을 등록하시면 알림이 자동 설정됩니다.",
                    createdAt = "2025-11-07T11:00:00Z",
                    isStaff = true
                )
            )
        ),

        // 🟢 3. 답변완료 (버그신고)
        Inquiry(
            id = 3,
            userId = 100,
            username = "사용자1",
            type = "버그신고",
            title = "로그인이 간헐적으로 안 됩니다",
            content = "앱 실행 후 로그인이 실패하는 경우가 있습니다.",
            isAnswered = true,
            createdAt = "2025-11-06T14:20:00Z",
            commentCount = 1,
            comments = listOf(
                InquiryComment(
                    id = 2,
                    inquiryId = 3,
                    userId = 999,
                    username = "관리자",
                    content = "현재 해당 이슈를 확인 중이며, 다음 업데이트에서 수정될 예정입니다.",
                    createdAt = "2025-11-06T15:10:00Z",
                    isStaff = true
                )
            )
        ),

        // 🟢 4. 답변완료
        Inquiry(
            id = 4,
            userId = 100,
            username = "사용자1",
            type = "일반문의",
            title = "문의 답변은 어디서 확인하나요?",
            content = "문의한 내용의 답변 확인 위치가 궁금합니다.",
            isAnswered = true,
            createdAt = "2025-11-05T09:40:00Z",
            commentCount = 1,
            comments = listOf(
                InquiryComment(
                    id = 3,
                    inquiryId = 4,
                    userId = 999,
                    username = "관리자",
                    content = "마이페이지 > 나의 문의 내역에서 확인하실 수 있습니다.",
                    createdAt = "2025-11-05T10:00:00Z",
                    isStaff = true
                )
            )
        )
    )


    val inquiriesState = remember { mutableStateListOf<Inquiry>().apply { addAll(sampleInquiries) } }
    val pagerState = rememberPagerState(initialPage = 0) { 2 }

    OnlyColorTheme {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = "문의사항",
                    showBack = true,
                    onBackClick = {}
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                FAQTabRow(pagerState = pagerState)
                HorizontalPager(state = pagerState) { index ->
                    when (index) {
                        0 -> InquiryHistory(inquiries = inquiriesState)
                        1 -> NewInquiryForm()
                    }
                }
            }
        }
    }
}
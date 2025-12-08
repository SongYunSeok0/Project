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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.shared.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.shared.bar.AppTopBar
import com.domain.model.Inquiry
import com.domain.model.InquiryComment
import com.mypage.viewmodel.MyPageViewModel
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
    viewModel: MyPageViewModel = hiltViewModel(),
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
    viewModel: MyPageViewModel = hiltViewModel()
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
        Inquiry(
            id = 1,
            userId = 100,
            username = "사용자1",
            type = "결제 문의",
            title = "환불은 어떻게 하나요?",
            content = "구매한 상품의 환불 절차가 궁금합니다.",
            isAnswered = true,
            createdAt = "2025-11-03T10:00:00Z",
            commentCount = 1,
            comments = listOf(
                InquiryComment(
                    id = 1,
                    inquiryId = 1,
                    userId = 999,
                    username = "관리자",
                    content = "환불은 구매일로부터 7일 이내 가능합니다.",
                    createdAt = "2025-11-04T14:00:00Z",
                    isStaff = true
                )
            )
        ),
        Inquiry(
            id = 2,
            userId = 100,
            username = "사용자1",
            type = "서비스 이용",
            title = "회원 탈퇴 방법 제목글자수테스트",
            content = "회원 탈퇴를 하고 싶은데 어떻게 하나요?",
            isAnswered = false,
            createdAt = "2025-11-05T10:00:00Z",
            commentCount = 0,
            comments = emptyList()
        ),
        Inquiry(
            id = 3,
            userId = 100,
            username = "사용자1",
            type = "기술 지원",
            title = "로그인이 안됩니다",
            content = "비밀번호를 입력해도 로그인이 되지 않아요.",
            isAnswered = true,
            createdAt = "2025-11-06T10:00:00Z",
            commentCount = 1,
            comments = listOf(
                InquiryComment(
                    id = 2,
                    inquiryId = 3,
                    userId = 999,
                    username = "관리자",
                    content = "비밀번호 재설정을 시도해보시기 바랍니다.",
                    createdAt = "2025-11-06T15:00:00Z",
                    isStaff = true
                )
            )
        ),
        Inquiry(
            id = 4,
            userId = 100,
            username = "사용자1",
            type = "기술 지원",
            title = "스크롤테스트12345678910",
            content = "비밀번호를 입력해도 로그인이 되지 않아요.",
            isAnswered = true,
            createdAt = "2025-11-07T10:00:00Z",
            commentCount = 1,
            comments = listOf(
                InquiryComment(
                    id = 3,
                    inquiryId = 4,
                    userId = 999,
                    username = "관리자",
                    content = "비밀번호 재설정을 시도해보시기 바랍니다.",
                    createdAt = "2025-11-07T16:00:00Z",
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
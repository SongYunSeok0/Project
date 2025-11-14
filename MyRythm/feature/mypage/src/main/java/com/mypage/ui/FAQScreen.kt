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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.design.AppTopBar
import com.domain.model.Inquiry
import com.mypage.viewmodel.MyPageViewModel
import com.ui.theme.OnlyColorTheme
import kotlinx.coroutines.launch

// 컴포넌트 적용했던 기존faq스크린을 2개로 분리, 이쪽은 기존 0번탭 그 외는 1번탭
// 0 -> InquiryHistory(inquiries)
@Composable
fun FAQScreen(
    //inquiries: List<Inquiry>,
    onSubmit: (type: String, title: String, content: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // rememberPagerState : 탭 간의 전환 상태 관리 용도
    val pagerState = rememberPagerState(initialPage = 0) { 2 }

    OnlyColorTheme {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                AppTopBar(
                    title = "문의사항",
                    showBack = true,
                    onBackClick = {}
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
            ) {
                FAQTabRow(pagerState = pagerState)

                FAQTabContent(
                    pagerState = pagerState,
                    //inquiries = inquiries,
                    onSubmit = onSubmit
                )
            }
        }
    }
}

// FAQTabRow 탭 레이아웃 영역. 나의 문의 내역 / 1:1 문의하기 탭 전환 컨테이너
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FAQTabRow(pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    val tabs = listOf("나의 문의 내역", "1:1 문의하기")

    PrimaryTabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = Color.Transparent,
        indicator = {   // TabIndicatorScope 안에서 tabPositions 사용 가능
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier
                    .tabIndicatorOffset(pagerState.currentPage),
                color = MaterialTheme.colorScheme.primary,
                height = 2.dp
            )
        },
        divider = {}  // 구분선 제거
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                selectedContentColor = Color(0xFF6AE0D9),
                unselectedContentColor = Color.Gray,
                modifier = Modifier.height(50.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    lineHeight = 1.5.em
                )
            }
        }
    }
}

// FAQTabContent 탭 안의 내용
@Composable
private fun FAQTabContent(
    pagerState: PagerState,
    //inquiries: List<Inquiry>,
    onSubmit: (type: String, title: String, content: String) -> Unit
) {

    // HorizontalPager: 좌우 스와이프 전환이 가능한 화면 구성
    HorizontalPager(state = pagerState) { index ->
        when (index) {
            0 -> InquiryHistory()
            1 -> NewInquiryForm(onSubmit)
        }
    }
}


@Composable
fun FAQScreenWrapper(
    viewModel: MyPageViewModel = hiltViewModel()
) {
    val inquiries by viewModel.inquiries.collectAsState()

    FAQScreen(
        /*inquiries = inquiries.map {
            com.domain.model.Inquiry(
                type = it.type,
                title = it.title,
                content = it.content,
                answer = it.answer
            )
        },*/
        onSubmit = { type, title, content ->
            viewModel.addInquiry(type, title, content)
        }
    )
}

// 0번 탭 - 나의 문의 내역 화면 (InquiryHistory)+컴포넌트 FAQInquiryCard.kt 호출
@Composable
private fun InquiryHistory(
    viewModel: MyPageViewModel = hiltViewModel()
) {
    val inquiries by viewModel.inquiries.collectAsState()

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


@Preview(showBackground = true)
@Composable
fun FAQScreenPreview() {
    val sampleInquiries = listOf(
        Inquiry(
            type = "결제 문의",
            title = "환불은 어떻게 하나요?",
            content = "구매한 상품의 환불 절차가 궁금합니다.",
            answer = "환불은 구매일로부터 7일 이내 가능합니다."
        ),
        Inquiry(
            type = "서비스 이용",
            title = "회원 탈퇴 방법",
            content = "회원 탈퇴를 하고 싶은데 어떻게 하나요?",
            answer = null
        ),
        Inquiry(
            type = "기술 지원",
            title = "로그인이 안됩니다",
            content = "비밀번호를 입력해도 로그인이 되지 않아요.",
            answer = "비밀번호 재설정을 시도해보시기 바랍니다."
        )
    )

    FAQScreen(
        //inquiries = sampleInquiries,
        onSubmit = { _, _, _ -> }
    )
}


/*
 //1113 수정전코드_컴포넌트코드적용이전
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domain.model.Inquiry
import com.mypage.viewmodel.MyPageViewModel

@Composable
fun FAQScreen(
    inquiries: List<Inquiry>,
    onSubmit: (type: String, title: String, content: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("history") }

    Column(modifier = modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
        ) {
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

@Preview(showBackground = true)
@Composable
fun FAQScreenPreview() {
    val sampleInquiries = listOf(
        Inquiry(
            type = "결제 문의",
            title = "환불은 어떻게 하나요?",
            content = "구매한 상품의 환불 절차가 궁금합니다.",
            answer = "환불은 구매일로부터 7일 이내 가능합니다."
        ),
        Inquiry(
            type = "서비스 이용",
            title = "회원 탈퇴 방법",
            content = "회원 탈퇴를 하고 싶은데 어떻게 하나요?",
            answer = null
        ),
        Inquiry(
            type = "기술 지원",
            title = "로그인이 안됩니다",
            content = "비밀번호를 입력해도 로그인이 되지 않아요.",
            answer = "비밀번호 재설정을 시도해보시기 바랍니다."
        )
    )

    FAQScreen(
        inquiries = sampleInquiries,
        onSubmit = { _, _, _ -> }
    )
}

*/
/*
@Preview(showBackground = true)
@Composable
fun FAQScreenEmptyPreview() {
    FAQScreen(
        inquiries = emptyList(),
        onSubmit = { _, _, _ -> }
    )
}

 */
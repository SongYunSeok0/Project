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
// 프리뷰는 FAQ스크린으로 확인하기
@Composable
fun FAQScreen(
    onSubmit: (type: String, title: String, content: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // rememberPagerState : 탭 간의 전환 상태 관리 용도
    val pagerState = rememberPagerState(initialPage = 0) { 2 }

    OnlyColorTheme {
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

// 뷰모델진입점
@Composable
fun FAQScreenWrapper(
    userId: String?,
    viewModel: MyPageViewModel = hiltViewModel()
) {
    FAQScreen(
        onSubmit = { type, title, content ->
            viewModel.addInquiry(type, title, content)
        }
    )
}

// 0번 탭 - 나의 문의 내역 화면 (InquiryHistory)+컴포넌트 FAQInquiryCard.kt 호출
@Composable
private fun InquiryHistory(
    inquiries: List<Inquiry>
) {
    //실제 코드에선 뷰모델로 연결하기
    //val inquiries by viewModel.inquiries.collectAsState()

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
    onSubmit: (type: String, title: String, content: String) -> Unit,
    viewModel: MyPageViewModel = hiltViewModel()
) {

    val inquiries by viewModel.inquiries.collectAsState()

    // HorizontalPager: 좌우 스와이프 전환이 가능한 화면 구성
    HorizontalPager(state = pagerState) { index ->
        when (index) {
            0 -> InquiryHistory(inquiries = inquiries)
            1 -> NewInquiryForm(onSubmit)
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
fun FAQScreenWithSampleDataPreview() {
    // 샘플 데이터
    val sampleInquiries = listOf(
        Inquiry(1, "결제 문의", "환불은 어떻게 하나요?", "구매한 상품의 환불 절차가 궁금합니다.", "환불은 구매일로부터 7일 이내 가능합니다."),
        Inquiry(2, "서비스 이용", "회원 탈퇴 방법 제목글자수테스트", "회원 탈퇴를 하고 싶은데 어떻게 하나요?", null),
        Inquiry(3, "기술 지원", "로그인이 안됩니다", "비밀번호를 입력해도 로그인이 되지 않아요.", "비밀번호 재설정을 시도해보시기 바랍니다."),
        Inquiry(4, "기술 지원", "스크롤테스트12345678910", "비밀번호를 입력해도 로그인이 되지 않아요.", "비밀번호 재설정을 시도해보시기 바랍니다."),
// 날짜 임시로 넣어둠, 도메인에 등록X상태. 이후 필요 시 추가 -> 인퀴어리.등록날짜 형태로 변경필요함
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
                        1 -> NewInquiryForm { type, title, content ->
                            inquiriesState.add(Inquiry(type = type, title = title, content = content))
                        }
                    }
                }
            }
        }
    }
}
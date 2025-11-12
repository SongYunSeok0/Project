package com.mypage.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.common.design.R
import com.design.AppTopBar
import com.ui.components.ImageAttachmentSection
import com.ui.components.Inquiry
import com.ui.components.InquiryCard
import com.ui.components.InquiryStatus
import com.ui.components.InquiryTextField
import com.ui.theme.OnlyColorTheme
import kotlinx.coroutines.launch
import kotlin.collections.filterIndexed


/*
ui 형태는 피그마 타입으로 진행하되 1:1 문의 및 그간의 나의 문의 내역+답변 달리는 형태로 게시글 수정

1. FAQ 스크린 파일 하나로 + 1:1 문의사항은 탭 전환 형태로(pdf 탭 전환 파트 확인)
    ㄴ 디테일 창은 마크다운으로 진행 예정이고 1:1 문의사항을 나누는 것보다 탭 전환으로 하는 게 ux면으로 편리하고 내비 하나만 사용 가능.
    ㄴ 이 경우 1:1 문의사항 남기다가 나의 문의 내역으로 탭 전환해도 적고 있던 문의사항 내용이 날아가지 않는다고 함
2. FAQ 기본 화면은 나의 문의내역 탭
    ㄴ 각 문의내역들 컨테이너가 마크다운 형태
 */


// 메인화면 영역(전체구조)
@Composable
fun FAQScreen(modifier: Modifier = Modifier) {

    // rememberPagerState : 탭 간의 전환 상태 관리 용도
    val pagerState = rememberPagerState(initialPage = 0) { 2 }
    OnlyColorTheme {
        // Scaffold 용도: 상단 탑바 - 탭 내용 구성
        Scaffold(
            modifier = modifier.fillMaxSize(),

            // 탑바 구역 - 나중에 컴포넌트 호출해서 쓰기+컬러테마씌워둠
            topBar = {
                AppTopBar(
                    title = "문의사항",
                    showBack = true,
                    onBackClick = {}
                )
            },

            // 전체 레이아웃 컬러
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)     // paddingValues: 스코폴드에서 탑바영역제외 자동여백계산(겹칩없게처리)
            ) {
                // 탭 레이아웃영역
                FAQTabRow(pagerState = pagerState)

                // 탭 컨텐츠
                FAQTabContent(pagerState = pagerState)
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
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier
                    .tabIndicatorOffset(pagerState.currentPage),
                color = MaterialTheme.colorScheme.primary,
                height = 2.dp
            )
        },

        divider = {}    // 구분선제거하는역할이라고함!
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selectedContentColor = Color(0xFF6AE0D9),
                unselectedContentColor = Color.Gray,
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)   // 페이지 전환 애니메이션 (코루틴스코프)
                    }
                },
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
private fun FAQTabContent(pagerState: PagerState) {

    // HorizontalPager: 좌우 스와이프 전환이 가능한 화면 구성
    HorizontalPager(state = pagerState) { index ->
        when (index) {
            0 -> InquiryHistoryScreen() //문의내역보기
            1 -> CreateInquiryScreen()  //문의작성하기
        }
    }
}

// 0번 탭 - 나의 문의 내역 화면 (InquiryHistoryScreen)+컴포넌트 FAQInquiryCard.kt 호출
@Composable
private fun InquiryHistoryScreen() {

    // 미리 정의한 샘플 문의 목록(Inquiry 리스트)을 불러와 LazyColumn으로 표시
    // 각 문의 항목은 InquiryCard()로 렌더링

    val inquiries = remember {  // 뷰모델 입력 시 val inquiries by viewModel.inquiries.collectAsState() 로 코드 변경됨
        listOf(
            Inquiry(1, "나의 문의 내역 제목", InquiryStatus.UNANSWERED, content = "내용 내용\n내용내용내용"),
            Inquiry(2, "처방전 인식이 잘 안돼요", InquiryStatus.ANSWERED, content = "내용 내용\n내용내용내용", answer = "답변\n답변완료\n내용내용"),
            Inquiry(3, "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15", InquiryStatus.ANSWERED, content = "내용 내용\n내용내용내용", answer = "답변\n답변완료\n내용내용"),
            Inquiry(4, "프로필 사진을 변경하고 싶어요", InquiryStatus.ANSWERED, content = "내용 내용\n내용내용내용", answer = "답변\n답변완료\n내용내용"),
            Inquiry(5, "복약 기록을 삭제하고 싶어요 제목 길이 테스트", InquiryStatus.ANSWERED, content = "내용 내용\n내용내용내용", answer = "답변\n답변완료\n내용내용")
        )
    }   // 현재는 하드코딩

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

// 1번탭 - 1:1 문의하기 - 1:1 문의 작성 화면 (CreateInquiryScreen) +컴포넌트 FAQInquiryInputField.kt 호출
@Composable
private fun CreateInquiryScreen() {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var images by remember { mutableStateOf<List<Uri>>(emptyList()) }
    OnlyColorTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "문의하기",
                fontSize = 20.sp,
                color = Color(0xFF101828),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // FAQInquiryInputField.kt 컴포넌트
            InquiryTextField(
                value = title,
                onValueChange = { title = it },
                label = "제목",
                singleLine = true,
                maxLines = 1
            )

            // FAQInquiryInputField.kt 컴포넌트
            InquiryTextField(
                value = content,
                onValueChange = { content = it },
                label = "문의 내용",
                height = 300.dp
            )


            // 이미지 첨부 섹션(컴포넌트화 시켰으나 코일 의존성 등 추가 필요)
            ImageAttachmentSection(
                images = images,
                onImagesSelected = { newImages ->
                    images = newImages
                },
                onImageRemove = { index ->
                    images = images.filterIndexed { i, _ -> i != index }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 문의하기 버튼 (추후 필요 시 AuthButton.kt처럼 기본 버튼 컴포넌트 만들거나 컴포넌트 같이 쓰기)
            Button(
                onClick = {
                    // 문의 제출 로직
                    // title, content, images 사용
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("문의하기", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
@Preview(widthDp = 412, heightDp = 917)
@Composable
private fun FAQScreenPreview() {
    FAQScreen(Modifier)
}
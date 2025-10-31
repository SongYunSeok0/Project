package com.news.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.common.design.R
import com.news.NewsViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen( // ← 네비그래프와 맞추기 위해 이름 변경
    nav: NavController,
    onOpenDetail: (String) -> Unit,
    viewModel: NewsViewModel = viewModel()
) {
    var selectedCategory by remember { mutableStateOf("건강") }
    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val pager = remember(selectedCategory) { viewModel.getNewsPager(selectedCategory) }
        .collectAsLazyPagingItems()


        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "오늘의 ${selectedCategory} 뉴스",
                fontSize = 16.sp,
                color = Color(0xFF3B566E),
                fontWeight = FontWeight.SemiBold
            )

            // 카테고리 배너
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BannerCard("건강 뉴스", "최신 건강 정보", Modifier.weight(1f)) {
                    selectedCategory = "건강"; pager.refresh()
                }
                BannerCard("의학 뉴스", "최신 의학 연구", Modifier.weight(1f)) {
                    selectedCategory = "의학"; pager.refresh()
                }
                BannerCard("복약 안전", "올바른 복용법", Modifier.weight(1f)) {
                    selectedCategory = "복약"; pager.refresh()
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("검색 (예: 감기 / 알레르기 / 복약)") },
                    modifier = Modifier
                        .weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF2F2F5),
                        unfocusedContainerColor = Color(0xFFF2F2F5),

                        focusedIndicatorColor = Color(0xFF3B3B3B),
                        unfocusedIndicatorColor = Color(0xFF3B3B3B),
                        disabledIndicatorColor = Color(0xFF3B3B3B),

                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedPlaceholderColor = Color(0xFF808080),
                        unfocusedPlaceholderColor = Color(0xFF808080)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(Modifier.width(8.dp))

                if (isSearchMode) {
                    TextButton(onClick = {
                        isSearchMode = false
                        searchQuery = ""
                        selectedCategory = "건강"
                        pager.refresh()
                    }) { Text("취소") }
                } else {
                    IconButton(onClick = { isSearchMode = true }) {
                        Icon(Icons.Default.Search, contentDescription = "검색")
                    }
                }
            }

            if (searchQuery.isNotBlank()) {
                LaunchedEffect(searchQuery) {
                    selectedCategory = searchQuery
                    pager.refresh()
                }
            }


            Text(
                text = "네이버 뉴스",
                fontSize = 16.sp,
                color = Color(0xFF3B566E),
                fontWeight = FontWeight.SemiBold
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(pager.itemSnapshotList.items) { item ->
                    item?.let {
                        // ✅ 안전한 URL 선택 (originallink 우선, 없으면 link)
                        val url = (it.originallink?.takeIf { s -> s.isNotBlank() } ?: it.link).trim()

                        // 타이틀의 HTML 태그 간단 정리
                        val cleanTitle = it.title
                            .replace("<b>", "")
                            .replace("</b>", "")
                            .replace("&quot;", "\"")

                        NewsCard(
                            title = cleanTitle,
                            info = it.pubDate.take(16),
                            imageUrl = it.image ?: "https://cdn-icons-png.flaticon.com/512/2965/2965879.png",
                            onClick = {
                                if (url.isNotEmpty()) {
                                    // ⚠️ 인코딩은 NavGraph에서 처리하지만, 이중 안전망을 원하면 아래 라인 사용
                                    // onOpenDetail(Uri.encode(url))
                                    onOpenDetail(url)
                                }
                            }
                        )
                    }
                }

                // 로딩 표시
                if (pager.loadState.refresh is LoadState.Loading ||
                    pager.loadState.append is LoadState.Loading
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                }
            }
        }

}

/* --------------------------- UI Components --------------------------- */

@Composable
fun BannerCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = R.drawable.photo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.6f)
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        }
    }
}

@Composable
fun NewsCard(
    title: String,
    info: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF3F4F6))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            Text(text = title, fontSize = 14.sp, color = Color(0xFF3B566E), fontWeight = FontWeight.Medium)
            Text(text = info, fontSize = 12.sp, color = Color(0xFF6F8BA4))
        }
    }
}

/* ------------------------------ Preview ------------------------------ */

@Preview(showBackground = true)
@Composable
fun NewsScreenPreview() {
    MaterialTheme {
        NewsMainScreen(
            nav = rememberNavController(),
            onOpenDetail = {}
        )
    }
}

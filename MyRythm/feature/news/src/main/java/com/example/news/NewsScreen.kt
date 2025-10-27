package com.example.news

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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.common.design.R
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.LoadState
import java.net.URLEncoder

/* ------------------------------ TopBar ------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsTopBar(
    isSearchMode: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onSearchCancel: () -> Unit,
    onSearchSubmit: () -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        windowInsets = WindowInsets(0, 0, 0, 0), // 인셋 수동 관리
        title = {
            if (isSearchMode) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("뉴스 검색...", color = Color.Gray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF009688)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("뉴스", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        navigationIcon = {
            onBackClick?.let {
                IconButton(onClick = it) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "뒤로가기",
                        tint = Color.Black
                    )
                }
            }
        },
        actions = {
            if (isSearchMode) {
                IconButton(onClick = onSearchCancel) {
                    Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.Black)
                }
            } else {
                IconButton(onClick = onSearchClicked) {
                    Icon(Icons.Default.Search, contentDescription = "검색", tint = Color.Black)
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFFDFF6F3),
            titleContentColor = Color.Black
        )
    )
}

/* ------------------------------ Screen ------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    navController: NavController,
    viewModel: NewsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: (() -> Unit)? = null
) {
    var selectedCategory by remember { mutableStateOf("건강") }
    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val pager = remember(selectedCategory) { viewModel.getNewsPager(selectedCategory) }
        .collectAsLazyPagingItems()

    val internalCanGoBack = navController.previousBackStackEntry != null
    val backHandler: (() -> Unit)? = when {
        onBack != null -> onBack
        internalCanGoBack -> { { navController.popBackStack() } }
        else -> null
    }

    Scaffold(
        containerColor = Color(0xFFF9F9FB),
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // 인셋 수동 관리
        topBar = {
            NewsTopBar(
                isSearchMode = isSearchMode,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchClicked = { isSearchMode = true },
                onSearchCancel = {
                    isSearchMode = false
                    searchQuery = ""
                    selectedCategory = "건강"
                },
                onSearchSubmit = {
                    if (searchQuery.isNotBlank()) {
                        selectedCategory = searchQuery
                        isSearchMode = false
                        pager.refresh()
                    }
                },
                onBackClick = backHandler
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
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

            Text(
                text = "네이버 뉴스",
                fontSize = 16.sp,
                color = Color(0xFF3B566E),
                fontWeight = FontWeight.SemiBold
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(pager.itemSnapshotList.items) { item ->
                    item?.let {
                        NewsCard(
                            title = it.title
                                .replace("<b>", "")
                                .replace("</b>", "")
                                .replace("&quot;", "\""),
                            info = it.pubDate.take(16),
                            imageUrl = it.image
                                ?: "https://cdn-icons-png.flaticon.com/512/2965/2965879.png",
                            onClick = {
                                val encodedUrl = URLEncoder.encode(it.link, "UTF-8")
                                navController.navigate("news_detail/$encodedUrl")
                            }
                        )
                    }
                }

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
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color(0xFF3B566E),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = info,
                fontSize = 12.sp,
                color = Color(0xFF6F8BA4)
            )
        }
    }
}

/* ------------------------------ Preview ------------------------------ */

@Preview(showBackground = true)
@Composable
fun NewsScreenPreview() {
    MaterialTheme {
        val nav = rememberNavController()
        NewsScreen(navController = nav, onBack = {})
    }
}

package com.news.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.common.design.R
import com.news.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    nav: NavController,
    onOpenDetail: (String) -> Unit,
    viewModel: NewsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchMode by viewModel.isSearchMode.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val pagingItems = viewModel.newsPagingFlow.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // -----------------------------------------------------------
        // 🔍 검색창 + ⭐ 즐겨찾기 추가 버튼
        // -----------------------------------------------------------
        AnimatedVisibility(visible = isSearchMode) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {

                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("검색어를 입력하세요") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { viewModel.triggerSearch() }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF2F2F5),
                        unfocusedContainerColor = Color(0xFFF2F2F5),
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (searchQuery.isNotBlank()) viewModel.addFavorite(searchQuery)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.bookmark),
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(6.dp))

                Button(
                    onClick = {
                        if (searchQuery.isNotBlank()) viewModel.triggerSearch()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6AE0D9))
                ) {
                    Text("검색", color = Color.White)
                }
            }
        }

        // -----------------------------------------------------------
        // ⭐ 즐겨찾기 배너카드
        // -----------------------------------------------------------
        Text(
            text = "즐겨찾기 검색",
            fontSize = 16.sp,
            color = Color(0xFF3B566E),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        FavoriteBannerCard(
            title = "즐겨찾기 키워드",
            favorites = favorites,
            onClickFavorite = { keyword ->
                viewModel.onFavoriteClick(keyword)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        // -----------------------------------------------------------
        // 📰 네이버 뉴스 리스트
        // -----------------------------------------------------------

        Text(
            text = "네이버 뉴스",
            fontSize = 16.sp,
            color = Color(0xFF3B566E),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            items(pagingItems.itemCount) { index ->
                val item = pagingItems[index] ?: return@items

                val cleanTitle = item.title
                    .replace("<b>", "")
                    .replace("</b>", "")
                    .replace("&quot;", "\"")
                    .replace("&apos;", "'")
                    .replace("&amp;", "&")

                NewsCard(
                    title = cleanTitle,
                    info = item.pubDate.take(16),
                    imageUrl = item.image ?: "https://cdn-icons-png.flaticon.com/512/2965/2965879.png",
                    onClick = {
                        onOpenDetail(Uri.encode(item.link))
                    }
                )
            }

            if (pagingItems.loadState.refresh is LoadState.Loading ||
                pagingItems.loadState.append is LoadState.Loading
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}



///////////////////////////////////////////////////////////////
// ⭐ FavoriteBannerCard (NewsScreen 안에 포함)
///////////////////////////////////////////////////////////////

@Composable
fun FavoriteBannerCard(
    title: String,
    favorites: List<com.domain.model.Favorite>,
    onClickFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
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

            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(favorites) { fav ->
                    AssistChip(
                        onClick = { onClickFavorite(fav.keyword) },
                        label = {
                            Text(
                                text = fav.keyword,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
        }
    }
}



///////////////////////////////////////////////////////////////
// 📰 NewsCard (NewsScreen 안에 포함)
///////////////////////////////////////////////////////////////

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
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
            Text(
                text = info,
                fontSize = 12.sp,
                color = Color(0xFF6F8BA4)
            )
        }
    }
}

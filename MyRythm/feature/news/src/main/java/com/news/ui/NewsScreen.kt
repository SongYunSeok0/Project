package com.news.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.news.NewsViewModel
import com.shared.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    nav: NavController,
    onOpenDetail: (String) -> Unit,
    viewModel: NewsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val favoritesText = stringResource(R.string.favorites)
    val naverNewsText = stringResource(R.string.naver_news)

    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchMode by viewModel.isSearchMode.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val pagingItems = viewModel.newsPagingFlow.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NewsSearchBar(
            searchQuery = searchQuery,
            isSearchMode = isSearchMode,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onSearch = { viewModel.triggerSearch() },
            onAddFavorite = { viewModel.addFavorite(searchQuery) }
        )

        Text(
            text = favoritesText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            items(favorites) { fav ->
                FavoriteBannerCard(
                    keyword = fav.keyword,
                    onClick = { viewModel.onFavoriteClick(fav.keyword) },
                    onDelete = { viewModel.removeFavorite(fav.keyword) },
                    modifier = Modifier.size(120.dp)
                )
            }
        }

        // -----------------------------------------------------------
        // 📰 네이버 뉴스 리스트
        // -----------------------------------------------------------
        Text(
            text = naverNewsText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    imageUrl = item.image
                        ?: "https://cdn-icons-png.flaticon.com/512/2965/2965879.png",
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
                    ) { CircularProgressIndicator() }
                }
            }
        }
    }
}
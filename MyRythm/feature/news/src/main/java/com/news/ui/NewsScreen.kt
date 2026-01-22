package com.news.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.news.NewsViewModel
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.components.AppInputField
import com.shared.ui.theme.AppFieldHeight
import com.shared.ui.theme.componentTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    nav: NavController,
    onOpenDetail: (String) -> Unit,
    viewModel: NewsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val searchMessage = stringResource(R.string.news_message_search)
    val searchText = stringResource(R.string.search)
    val favoritesText = stringResource(R.string.favorites)
    val naverNewsText = stringResource(R.string.naver_news)
    val bookMarkText = stringResource(R.string.bookmark)
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchMode by viewModel.isSearchMode.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val pagingItems = viewModel.newsPagingFlow.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(visible = isSearchMode) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                AppInputField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    label = searchMessage,
                    singleLine = true,
                    imeAction = ImeAction.Search,
                    keyboardActions = KeyboardActions(
                        onSearch = { viewModel.triggerSearch() }
                    ),
                    outlined = false,
                    modifier = Modifier.weight(7f)
                )

                Spacer(Modifier.width(8.dp))

                AppButton(
                    text = searchText,
                    onClick = {
                        if (searchQuery.isNotBlank()) viewModel.triggerSearch()
                    },
                    height = AppFieldHeight,
                    modifier = Modifier.weight(1.5f)
                )

                Spacer(Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (searchQuery.isNotBlank()) viewModel.addFavorite(searchQuery)
                    },
                    modifier = Modifier.weight(1.5f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.bookmark),
                        contentDescription = bookMarkText,
                        tint = MaterialTheme.componentTheme.bookMarkColor
                    )
                }
            }
        }

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


@Composable
fun FavoriteBannerCard(
    keyword: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val removeFavoriteText = stringResource(R.string.remove_favorite)
    val photoText = stringResource(R.string.photo)

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = R.drawable.photo),
            contentDescription = photoText,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    )
                )
        )
        IconButton(
            onClick = { onDelete() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.bookmark_remove),
                contentDescription = removeFavoriteText,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = keyword,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        )
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
            .clip(MaterialTheme.shapes.medium)
            .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .clip(MaterialTheme.shapes.small),
            contentScale = ContentScale.Crop
        )
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Text(
                text = info,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

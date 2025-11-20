package com.data.network.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.data.network.api.NewsApi
import com.data.network.dto.news.NaverNewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class NaverNewsPagingSource(
    private val api: NewsApi,
    private val query: String
) : PagingSource<Int, NaverNewsItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NaverNewsItem> {
        return try {
            val page = params.key ?: 1
            val display = 10
            val start = (page - 1) * display + 1

            val response = api.getNews(
                query = query,
                display = display,
                start = start,
                sort = "date"
            )

            // 🔥 각 뉴스 항목마다 이미지 크롤링 진행
            val itemsWithImage = response.items.map { item ->
                val imageUrl = fetchThumbnail(item.link)

                item.copy(
                    image = imageUrl // DTO에 image 필드 있지? 거기로 넣어줌
                )
            }

            LoadResult.Page(
                data = itemsWithImage,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.items.isEmpty()) null else page + 1
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, NaverNewsItem>): Int? {
        return state.anchorPosition?.let { pos ->
            val anchorPage = state.closestPageToPosition(pos)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    // 🔥 실제 썸네일 가져오는 함수
    private suspend fun fetchThumbnail(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(3000)
                .get()

            val meta = doc.select("meta[property=og:image]").attr("content")
            if (meta.isNotEmpty()) meta else null
        } catch (e: Exception) {
            null
        }
    }
}

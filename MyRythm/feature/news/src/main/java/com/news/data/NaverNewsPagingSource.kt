package com.news.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.news.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

//data 모듈의 repository로 옮겨야함
class NaverNewsPagingSource(private val query: String) :
    PagingSource<Int, NaverNewsItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NaverNewsItem> {
        return try {
            val page = params.key ?: 1
            val display = 10
            val start = (page - 1) * display + 1

            // ✅ 네이버 뉴스 검색 API 호출
            val response = RetrofitInstance.api.getNews(
                clientId = BuildConfig.NAVER_CLIENT_ID,
                clientSecret = BuildConfig.NAVER_CLIENT_SECRET,
                query = query,
                display = display,
                sort = "date"
            )

            // ✅ 각 뉴스의 링크에서 이미지(og:image) 추출
            val newsWithImages = response.items.map { item ->
                val imageUrl = fetchThumbnail(item.link)
                item.copy(image = imageUrl)
            }

            LoadResult.Page(
                data = newsWithImages,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.items.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, NaverNewsItem>): Int? {
        return state.anchorPosition?.let { anchor ->
            val anchorPage = state.closestPageToPosition(anchor)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    // ✅ og:image 메타 태그에서 썸네일 URL 추출
    private suspend fun fetchThumbnail(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url).get()
            val metaTag = doc.select("meta[property=og:image]").attr("content")
            if (metaTag.isNotEmpty()) metaTag else null
        } catch (e: Exception) {
            null
        }
    }
}

package com.news

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.news.data.RetrofitInstance
import com.news.data.NaverNewsItem
import com.news.data.NaverNewsPagingSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class NewsViewModel : ViewModel() {

    // ✅ Paging3 (무한스크롤용)
    fun getNewsPager(query: String) = Pager(
        PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        )
    ) {
        NaverNewsPagingSource(query)
    }.flow.cachedIn(viewModelScope)

    // ✅ 일반 리스트 (기존 loadNews 용)
    private val _newsList = MutableStateFlow<List<NaverNewsItem>>(emptyList())
    val newsList: StateFlow<List<NaverNewsItem>> = _newsList

    fun loadNews(query: String = "건강") {
        viewModelScope.launch {
            try {
                Log.d("NaverNews", "뉴스 API 요청: $query")

                val response = RetrofitInstance.api.getNews(
                    clientId = BuildConfig.NAVER_CLIENT_ID,
                    clientSecret = BuildConfig.NAVER_CLIENT_SECRET,
                    query = query
                )

                val newsWithImages = response.items.map { item ->
                    val imageUrl = fetchThumbnail(item.link)
                    item.copy(image = imageUrl)
                }

                _newsList.value = newsWithImages
                Log.d("NaverNews", "✅ '${query}' 뉴스 ${newsWithImages.size}개 로드 완료")

            } catch (e: Exception) {
                Log.e("NaverNews", "❌ API 호출 중 오류", e)
            }
        }
    }

    // ✅ 기사 본문에서 og:image 추출
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

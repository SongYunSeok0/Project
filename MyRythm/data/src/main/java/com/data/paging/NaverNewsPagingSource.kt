package com.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.data.remote.api.NaverNewsApi
import com.data.remote.dto.NewsItem
import javax.inject.Inject

class NaverNewsPagingSource(
    private val api: NaverNewsApi,
    private val query: String
) : PagingSource<Int, NewsItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NewsItem> {
        return try {
            val page = params.key ?: 1
            val start = (page - 1) * 10 + 1
            val response = api.getNews(query = query, start = start, display = 10)
            val items = response.items
            LoadResult.Page(
                data = items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (items.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, NewsItem>): Int? = null

    class Factory @Inject constructor(private val api: NaverNewsApi) {
        fun create(query: String): NaverNewsPagingSource = NaverNewsPagingSource(api, query)
    }
}

package com.data.network.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.data.network.api.NewsApi
import com.data.network.dto.news.NaverNewsItem

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

            LoadResult.Page(
                data = response.items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.items.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, NaverNewsItem>): Int? {
        // 가장 기본적인 구현
        return state.anchorPosition?.let { pos ->
            val anchorPage = state.closestPageToPosition(pos)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}

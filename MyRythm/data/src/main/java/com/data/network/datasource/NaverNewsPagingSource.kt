package com.data.network.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.data.network.dto.news.NaverNewsItem


class NaverNewsPagingSource(
    private val remoteDataSource: NewsRemoteDataSource,
    private val htmlParser: NewsHtmlParser,
    private val query: String
) : PagingSource<Int, NaverNewsItem>() {

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, NaverNewsItem> {

        return try {
            val page = params.key ?: 1
            val display = 10
            val start = (page - 1) * display + 1

            val response =
                remoteDataSource.fetchNews(query, display, start)

            val items = response.items.map { item ->
                val image = htmlParser.fetchThumbnail(item.link)
                item.copy(image = image)
            }

            LoadResult.Page(
                data = items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (items.isEmpty()) null else page + 1
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(
        state: PagingState<Int, NaverNewsItem>
    ): Int? =
        state.anchorPosition?.let { pos ->
            state.closestPageToPosition(pos)
                ?.prevKey?.plus(1)
                ?: state.closestPageToPosition(pos)
                    ?.nextKey?.minus(1)
        }
}


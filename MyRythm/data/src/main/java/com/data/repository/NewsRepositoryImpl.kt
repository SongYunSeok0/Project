package com.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.data.mapper.toDomain
import com.data.network.api.NewsApi
import com.data.network.datasource.NaverNewsPagingSource
import com.data.network.dto.news.NaverNewsItem
import com.domain.model.News
import com.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

class NewsRepositoryImpl @Inject constructor(
    private val api: NewsApi
) : NewsRepository {

    override fun getNews(query: String): Flow<PagingData<News>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = {
                NaverNewsPagingSource(api, query)
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }
}


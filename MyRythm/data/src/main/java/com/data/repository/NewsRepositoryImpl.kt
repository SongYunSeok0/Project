package com.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.data.mapper.toDomain
import com.data.network.datasource.NaverNewsPagingSource
import com.data.network.datasource.NewsHtmlParser
import com.data.network.datasource.NewsRemoteDataSource
import com.domain.model.News
import com.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val remoteDataSource: NewsRemoteDataSource,
    private val htmlParser: NewsHtmlParser
) : NewsRepository {

    override fun getNews(query: String): Flow<PagingData<News>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = {
                NaverNewsPagingSource(
                    remoteDataSource = remoteDataSource,
                    htmlParser = htmlParser,
                    query = query
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }
}



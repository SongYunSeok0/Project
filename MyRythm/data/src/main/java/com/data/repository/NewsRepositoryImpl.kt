package com.data.repository

import com.data.mapper.toDomain
import com.data.network.datasource.NewsHtmlParser
import com.data.network.datasource.NewsRemoteDataSource
import com.domain.model.News
import com.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val remoteDataSource: NewsRemoteDataSource,
    private val htmlParser: NewsHtmlParser
) : NewsRepository {

    override fun getNews(query: String): Flow<List<News>> = flow {
        val display = 10
        val start = 1

        val response = remoteDataSource.fetchNews(
            query = query,
            display = display,
            start = start
        )

        val items = response.items.map { item ->
            val image = htmlParser.fetchThumbnail(item.link)
            item.copy(image = image)
        }

        emit(items.map { it.toDomain() })
    }
}

package com.data.repository

import com.domain.model.News
import com.domain.repository.NewsRepository


class NewsRepositoryImpl(
    private val getNewsRemote: suspend () -> List<News>
) : NewsRepository {
    override suspend fun getNews(): List<News> = getNewsRemote()
}

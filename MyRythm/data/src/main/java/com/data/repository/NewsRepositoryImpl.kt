package com.data.repository

import com.myrythm.domain.model.News
import com.myrythm.domain.repository.NewsRepository


class NewsRepositoryImpl(
    private val getNewsRemote: suspend () -> List<News>
) : NewsRepository {
    override suspend fun getNews(): List<News> = getNewsRemote()
}

package com.domain.repository

import com.domain.model.News
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun getNews(query: String): Flow<List<News>>
}

package com.domain.repository

import com.domain.model.News

interface NewsRepository {
    suspend fun getNews(): List<News>
}

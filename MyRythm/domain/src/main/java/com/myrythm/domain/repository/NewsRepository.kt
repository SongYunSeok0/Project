
package com.myrythm.domain.repository

import com.myrythm.domain.model.News

interface NewsRepository {
    suspend fun getNews(): List<News>
}

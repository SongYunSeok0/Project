package com.domain.usecase.news

import com.domain.model.News
import com.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(query: String): Flow<List<News>> {
        return repository.getNews(query)
    }
}
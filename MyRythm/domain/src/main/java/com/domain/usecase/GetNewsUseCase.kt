package com.domain.usecase

import androidx.paging.PagingData
import com.domain.model.News
import com.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(query: String): Flow<PagingData<News>> {
        return repository.getNews(query)
    }
}

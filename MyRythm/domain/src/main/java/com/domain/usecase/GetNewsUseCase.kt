package com.domain.usecase

import androidx.paging.PagingData
import com.domain.model.News
import com.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow


class GetNewsUseCase(
    private val newsRepository: NewsRepository
) {
    operator fun invoke(query: String): Flow<PagingData<News>> {
        return newsRepository.getNewsPager(query)
    }
}

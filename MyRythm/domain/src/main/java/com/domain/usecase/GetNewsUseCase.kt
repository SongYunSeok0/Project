package com.domain.usecase

import com.domain.repository.NewsRepository

class GetNewsUseCase(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke() = newsRepository.getNews()
}



package com.myrythm.domain.usecase

import com.myrythm.domain.model.News
import com.myrythm.domain.repository.NewsRepository



class GetNewsUseCase(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke() = newsRepository.getNews()
}


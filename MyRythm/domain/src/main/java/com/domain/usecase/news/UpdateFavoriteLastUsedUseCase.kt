package com.domain.usecase.news

import com.domain.repository.FavoriteRepository
import javax.inject.Inject

class UpdateFavoriteLastUsedUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(keyword: String) {
        favoriteRepository.updateLastUsed(keyword)
    }
}

package com.domain.usecase.news

import com.domain.repository.FavoriteRepository
import javax.inject.Inject

class RemoveFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(keyword: String, userId: String) {
        favoriteRepository.deleteFavorite(keyword, userId)
    }
}

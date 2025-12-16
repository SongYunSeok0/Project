package com.domain.usecase.news

import com.domain.model.Favorite
import com.domain.repository.FavoriteRepository
import javax.inject.Inject

class AddFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(favorite: Favorite) {
        favoriteRepository.insertFavorite(favorite)
    }
}

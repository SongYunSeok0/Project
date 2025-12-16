package com.domain.usecase.news

import com.domain.model.Favorite
import com.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    operator fun invoke(): Flow<List<Favorite>> {
        return favoriteRepository.getFavorites()
    }
}

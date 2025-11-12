package com.domain.repository

import com.domain.model.Favorite
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    suspend fun insertFavorite(favorite: Favorite)
    suspend fun deleteFavorite(keyword: String, userId: String)
    fun getFavorites(userId: String): Flow<List<Favorite>>
    suspend fun isFavorite(keyword: String, userId: String): Boolean
}

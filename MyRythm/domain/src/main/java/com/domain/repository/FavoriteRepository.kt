package com.domain.repository

import com.domain.model.Favorite
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    suspend fun insertFavorite(favorite: Favorite)
    suspend fun deleteFavorite(keyword: String,userId: String)
    suspend fun isFavorite(keyword: String): Boolean
    fun getFavorites(): Flow<List<Favorite>>
    suspend fun updateLastUsed(keyword: String)
}

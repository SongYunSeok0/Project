package com.data.repository

import com.data.db.dao.FavoriteDao
import com.data.mapper.toDomain
import com.data.mapper.toEntity
import com.domain.model.Favorite
import com.domain.repository.FavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val dao: FavoriteDao
) : FavoriteRepository {

    override suspend fun insertFavorite(favorite: Favorite) = withContext(Dispatchers.IO) {
        dao.insertFavorite(favorite.toEntity())
    }

    override suspend fun deleteFavorite(keyword: String, userId: String) = withContext(Dispatchers.IO) {
        dao.deleteFavorite(keyword, userId)
    }

    override fun getFavorites(userId: String): Flow<List<Favorite>> =
        dao.getFavorites(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun isFavorite(keyword: String, userId: String): Boolean =
        withContext(Dispatchers.IO) { dao.isFavorite(keyword, userId) }
}

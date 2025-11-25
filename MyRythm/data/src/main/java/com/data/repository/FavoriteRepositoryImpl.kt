package com.data.repository

import com.data.db.dao.FavoriteDao
import com.data.mapper.toDomain
import com.data.mapper.toEntity
import com.domain.model.Favorite
import com.domain.repository.FavoriteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val dao: FavoriteDao,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : FavoriteRepository {

    override suspend fun insertFavorite(favorite: Favorite) =
        withContext(io) {
            dao.insertFavorite(favorite.toEntity())
        }

    override suspend fun deleteFavorite(keyword: String,userId: String) =
        withContext(io) {
            dao.deleteFavorite(keyword,userId)
        }

    override suspend fun isFavorite(keyword: String): Boolean =
        withContext(io) {
            dao.isFavorite(keyword)
        }

    override fun getFavorites(): Flow<List<Favorite>> =
        dao.getFavorites()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun updateLastUsed(keyword: String) =
        withContext(io) {
            dao.updateLastUsed(keyword, System.currentTimeMillis())
        }
}

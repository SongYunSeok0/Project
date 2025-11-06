package com.auth.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FavoriteRepository(private val dao: FavoriteDao) {

    // ✅ 즐겨찾기 추가
    suspend fun insertFavorite(favorite: FavoriteEntity) = withContext(Dispatchers.IO) {
        dao.insertFavorite(favorite)
    }

    // ✅ 즐겨찾기 삭제
    suspend fun deleteFavorite(keyword: String, userId: String) = withContext(Dispatchers.IO) {
        dao.deleteFavorite(keyword, userId)
    }

    // ✅ 즐겨찾기 목록 조회
    fun getFavorites(userId: String): Flow<List<FavoriteEntity>> {
        return dao.getFavorites(userId)
    }

    // ✅ 특정 키워드가 즐겨찾기인지 확인
    suspend fun isFavorite(keyword: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        dao.isFavorite(keyword, userId)
    }
}

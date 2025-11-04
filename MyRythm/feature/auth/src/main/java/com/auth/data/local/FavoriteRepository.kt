//package com.news.data
//
//import com.news.data.local.FavoriteDao
//import com.news.data.local.FavoriteEntity
//import kotlinx.coroutines.flow.Flow
//
//class FavoriteRepository(private val dao: FavoriteDao) {
//    fun getFavorites(): Flow<List<FavoriteEntity>> = dao.getAllFavorites()
//    suspend fun addFavorite(keyword: String) = dao.insertFavorite(FavoriteEntity(keyword = keyword))
//    suspend fun removeFavorite(keyword: String) = dao.deleteFavorite(FavoriteEntity(keyword = keyword))
//}

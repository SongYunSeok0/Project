// data/.../mapper/FavoriteMapper.kt
package com.data.mapper

import com.data.db.entity.FavoriteEntity
import com.domain.model.Favorite

fun FavoriteEntity.toDomain() = Favorite(
    id = id,
    keyword = keyword,
    userId = userId,
    timestamp = timestamp
)

fun Favorite.toEntity(): FavoriteEntity = FavoriteEntity(
    id = id,
    keyword = keyword,
    userId = userId,
    timestamp = if (timestamp > 0) timestamp else System.currentTimeMillis()
)

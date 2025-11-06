package com.data.network.mapper

import com.data.db.entity.FavoriteEntity
import com.domain.model.Favorite

fun FavoriteEntity.toDomain() = Favorite(
    id = id, keyword = keyword, userId = userId, timestamp = timestamp
)

fun Favorite.toEntity() = FavoriteEntity(
    id = id, keyword = keyword, userId = userId, timestamp = timestamp
)

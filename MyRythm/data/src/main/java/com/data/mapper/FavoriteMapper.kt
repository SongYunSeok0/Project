package com.data.mapper

import com.data.db.entity.FavoriteEntity
import com.domain.model.Favorite

fun FavoriteEntity.toDomain(): Favorite =
    Favorite(
        id = id,
        keyword = keyword,
        timestamp = timestamp,
        lastUsed = lastUsed
    )

fun Favorite.toEntity(): FavoriteEntity =
    FavoriteEntity(
        id = id,
        keyword = keyword,
        timestamp = timestamp,
        lastUsed = lastUsed
    )

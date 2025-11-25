package com.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val keyword: String,
    val timestamp: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis(),
    val userId: String
)
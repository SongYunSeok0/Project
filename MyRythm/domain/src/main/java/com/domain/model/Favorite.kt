package com.domain.model

data class Favorite(
    val id: Long = 0L,
    val keyword: String,
    val timestamp: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis()
)


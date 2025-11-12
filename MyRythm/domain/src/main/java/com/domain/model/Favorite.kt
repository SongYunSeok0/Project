package com.domain.model

data class Favorite(
    val id: Long = 0L,
    val keyword: String,
    val userId: String,
    val timestamp: Long
)

package com.data.network.dto.news

data class NaverNewsResponse(
    val items: List<NaverNewsItem>
)

data class NaverNewsItem(
    val title: String,
    val originallink: String,
    val link: String,
    val description: String,
    val pubDate: String,
    val image: String? = null
)

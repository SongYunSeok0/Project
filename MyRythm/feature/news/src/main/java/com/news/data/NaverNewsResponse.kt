package com.news.data

//data모듈의 remote/dto 로 옮겨야함
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

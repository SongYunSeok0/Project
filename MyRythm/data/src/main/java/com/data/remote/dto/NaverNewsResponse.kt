package com.data.remote.dto


//data모듈의 remote/dto 로 옮겨야함
data class NaverNewsResponse(
    val items: List<NewsItem>
)

data class NewsItem(
    val title: String,
    val originallink: String,
    val link: String,
    val description: String,
    val pubDate: String,
    val image: String? = null
)

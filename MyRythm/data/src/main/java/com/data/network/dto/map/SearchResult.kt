package com.data.network.dto.map

data class SearchResult(
    val lastBuildDate: String? = null,
    val total: Int = 0,
    val start: Int = 1,
    val display: Int = 0,
    val items: List<PlaceItem> = emptyList()
)
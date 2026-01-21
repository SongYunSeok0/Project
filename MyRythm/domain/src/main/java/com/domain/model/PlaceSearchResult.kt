package com.domain.model

data class PlaceSearchResult(
    val total: Int,
    val start: Int,
    val display: Int,
    val places: List<Place>
)
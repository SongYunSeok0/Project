package com.data.network.dto.map

data class PlaceItem(
    val title: String,
    val link: String? = null,
    val category: String? = null,
    val telephone: String? = null,
    val address: String,
    val roadAddress: String? = null,
    val mapx: String,
    val mapy: String
)
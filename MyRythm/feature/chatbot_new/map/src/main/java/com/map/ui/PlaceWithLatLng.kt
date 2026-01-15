package com.map.ui

import com.naver.maps.geometry.LatLng

data class PlaceWithLatLng(
    val title: String,
    val address: String,
    val category: String?,
    val telephone: String?,
    val roadAddress: String?,
    val link: String?,
    val position: LatLng
)
package com.map.mapper

import com.domain.model.Location
import com.domain.model.Place
import com.map.ui.PlaceWithLatLng
import com.naver.maps.geometry.LatLng

fun Location.toNaverLatLng() = LatLng(latitude, longitude)

fun LatLng.toDomainLocation() = Location(
    latitude = latitude,
    longitude = longitude
)

fun Place.toUiModel() = PlaceWithLatLng(
    title = title,
    address = address,
    category = category,
    telephone = telephone,
    roadAddress = roadAddress,
    link = link,
    position = location.toNaverLatLng()
)
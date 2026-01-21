package com.data.mapper

import com.data.network.dto.map.PlaceItem
import com.data.util.cleanHtml
import com.domain.model.Location
import com.domain.model.Place
import com.naver.maps.geometry.LatLng

// Naver Maps LatLng → Domain Location
fun LatLng.toDomainLocation() = Location(
    latitude = latitude,
    longitude = longitude
)

// Domain Location → Naver Maps LatLng
fun Location.toNaverLatLng() = LatLng(
    latitude,
    longitude
)

// PlaceItem (DTO) + LatLng → Domain Place
fun PlaceItem.toDomain(latLng: LatLng) = Place(
    title = cleanHtml(title),
    category = category,
    address = address,
    roadAddress = roadAddress,
    telephone = telephone,
    location = latLng.toDomainLocation(),
    link = link
)
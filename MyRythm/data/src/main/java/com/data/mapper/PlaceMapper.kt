package com.data.mapper

import com.data.network.dto.map.PlaceItem
import com.data.util.cleanHtml
import com.domain.model.Location
import com.domain.model.Place


fun PlaceItem.toDomain(location: Location) = Place(
    title = cleanHtml(title),
    category = category,
    address = address,
    roadAddress = roadAddress,
    telephone = telephone,
    location = location,
    link = link
)
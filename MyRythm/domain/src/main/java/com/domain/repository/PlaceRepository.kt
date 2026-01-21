package com.domain.repository

import com.domain.model.ApiResult
import com.domain.model.Location
import com.domain.model.Place

interface PlaceRepository {
    suspend fun searchPlaces(
        query: String,
        center: Location,
        mode: String,
        radiusMeters: Int = 1500
    ): ApiResult<List<Place>>
}
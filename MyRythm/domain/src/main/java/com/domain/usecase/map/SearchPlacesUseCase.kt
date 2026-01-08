package com.domain.usecase

import com.domain.model.ApiResult
import com.domain.model.Location
import com.domain.model.Place
import com.domain.repository.PlaceRepository
import javax.inject.Inject

class SearchPlacesUseCase @Inject constructor(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke(
        query: String,
        center: Location,
        mode: String,
        radiusMeters: Int = 1500
    ): ApiResult<List<Place>> {
        return placeRepository.searchPlaces(query, center, mode, radiusMeters)
    }
}
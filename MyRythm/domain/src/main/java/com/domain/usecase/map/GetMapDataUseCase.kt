package com.domain.usecase.map

import com.domain.model.MapData
import com.domain.repository.MapRepository

class GetMapDataUseCase (
    private val repository: MapRepository
) {
    suspend operator fun invoke(): List<MapData> = repository.getMapData()
}
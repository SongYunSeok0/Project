
package com.myrythm.domain.usecase

import com.myrythm.domain.model.MapData
import com.myrythm.domain.repository.MapRepository

class GetMapDataUseCase (
    private val repository: MapRepository
) {
    suspend operator fun invoke(): List<MapData> = repository.getMapData()
}

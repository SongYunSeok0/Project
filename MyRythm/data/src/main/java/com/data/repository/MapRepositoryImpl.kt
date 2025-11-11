package com.data.repository

import com.domain.model.MapData
import com.domain.repository.MapRepository

class MapRepositoryImpl(
    private val getMapDataRemote: suspend () -> List<MapData>
) : MapRepository {
    override suspend fun getMapData(): List<MapData> = getMapDataRemote()
}

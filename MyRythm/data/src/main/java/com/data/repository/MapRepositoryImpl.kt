package com.data.repository

import com.myrythm.domain.model.MapData
import com.myrythm.domain.repository.MapRepository

class MapRepositoryImpl(
    private val getMapDataRemote: suspend () -> List<MapData>
) : MapRepository {
    override suspend fun getMapData(): List<MapData> = getMapDataRemote()
}

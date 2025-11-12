package com.domain.repository

import com.domain.model.MapData

interface MapRepository {
    suspend fun getMapData(): List<MapData>
}

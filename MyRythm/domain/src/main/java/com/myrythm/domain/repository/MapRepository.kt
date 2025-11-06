
package com.myrythm.domain.repository

import com.myrythm.domain.model.MapData

interface MapRepository {
    suspend fun getMapData(): List<MapData>
}

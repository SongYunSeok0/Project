package com.myrythm.remote.api

import retrofit2.http.GET
import com.myrythm.domain.model.MapData

interface MapApi {
    @GET("map")
    suspend fun getMapData(): List<MapData>
}
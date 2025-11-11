package com.data.network.api

import retrofit2.http.GET
import com.domain.model.MapData

interface MapApi {
    @GET("map")
    suspend fun getMapData(): List<MapData>
}
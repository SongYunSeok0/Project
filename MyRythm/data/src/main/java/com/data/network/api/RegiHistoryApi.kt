package com.data.network.api

import com.data.network.dto.regihistory.RegiHistoryRequest
import com.data.network.dto.regihistory.RegiHistoryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RegiHistoryApi {

    @POST("regihistory/")
    suspend fun createRegiHistory(
        @Body body: RegiHistoryRequest
    ): RegiHistoryResponse

    @GET("regihistory/")
    suspend fun getRegiHistories(): List<RegiHistoryResponse>
}

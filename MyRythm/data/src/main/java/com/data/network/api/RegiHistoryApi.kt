package com.data.network.api

import com.data.network.dto.regihistory.RegiHistoryRequest
import com.data.network.dto.regihistory.RegiHistoryResponse
import retrofit2.Response
import retrofit2.http.*

interface RegiHistoryApi {

    @POST("regihistory/")
    suspend fun createRegiHistory(
        @Body body: RegiHistoryRequest
    ): RegiHistoryResponse

    @GET("regihistory/")
    suspend fun getRegiHistories(): List<RegiHistoryResponse>

    @PATCH("regihistory/{id}/")
    suspend fun updateRegiHistory(
        @Path("id") id: Long,
        @Body body: RegiHistoryRequest
    ): RegiHistoryResponse

    @DELETE("regihistory/{id}/delete/")
    suspend fun deleteRegiHistory(
        @Path("id") id: Long
    ): Response<Unit>
}

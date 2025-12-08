package com.data.network.api

import com.data.network.dto.regihistory.RegiHistoryRequest
import com.data.network.dto.regihistory.RegiHistoryResponse
import com.data.network.dto.regihistory.RegiHistoryWithPlansResponse
import retrofit2.Response
import retrofit2.http.*

interface RegiHistoryApi {

    @POST("med/regihistory/")
    suspend fun createRegiHistory(
        @Body body: RegiHistoryRequest
    ): RegiHistoryResponse

    @GET("med/regihistory/")
    suspend fun getRegiHistories(): List<RegiHistoryResponse>

    @PATCH("med/regihistory/{id}/")
    suspend fun updateRegiHistory(
        @Path("id") id: Long,
        @Body body: RegiHistoryRequest
    ): RegiHistoryResponse

    @DELETE("med/regihistory/{id}/delete/")
    suspend fun deleteRegiHistory(
        @Path("id") id: Long
    ): Response<Unit>

    // 🔥 스태프 전용: 특정 사용자의 모든 등록 이력 (Plan 포함)
    @GET("med/regihistory/user/{userId}/")
    suspend fun getUserRegiHistories(
        @Path("userId") userId: Long
    ): List<RegiHistoryWithPlansResponse>

    // 🔥 스태프 전용: 모든 등록 이력
    @GET("med/regihistory/all/")
    suspend fun getAllRegiHistories(): List<RegiHistoryWithPlansResponse>
}
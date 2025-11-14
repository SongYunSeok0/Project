package com.data.network.api

import com.data.network.dto.plan.IdResponse
import com.data.network.dto.plan.PlanCreateRequest
import com.data.network.dto.plan.PlanResponse
import com.data.network.dto.plan.PlanUpdateRequest
import retrofit2.http.*
interface PlanApi {
    @GET("plan/{userId}")
    suspend fun getPlans(@Path("userId") userId: Long): List<PlanResponse>

    @POST("plan/")
    suspend fun createPlan(@Body request: PlanCreateRequest): IdResponse

    @PUT("plan/{id}/")
    suspend fun updatePlan(@Path("id") id: Long, @Body request: PlanUpdateRequest)

    @DELETE("plan/{id}/")
    suspend fun deletePlan(@Path("id") id: Long)
}

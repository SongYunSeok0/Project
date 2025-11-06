package com.data.network.api

import com.data.network.dto.plan.IdResponse
import com.data.network.dto.plan.PlanCreateRequest
import com.data.network.dto.plan.PlanResponse
import com.data.network.dto.plan.PlanUpdateRequest
import retrofit2.http.*
interface PlanApi {
    @GET("plans")
    suspend fun getPlans(): List<PlanResponse>

    @POST("plans")
    suspend fun createPlan(@Body body: PlanCreateRequest): IdResponse // ← 반환 명시

    @PUT("plans/{id}")
    suspend fun updatePlan(@Path("id") id: Long, @Body body: PlanUpdateRequest)

    @DELETE("plans/{id}")
    suspend fun deletePlan(@Path("id") id: Long)
}

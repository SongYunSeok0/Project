package com.data.network.api

import com.data.network.dto.plan.IdResponse
import com.data.network.dto.plan.PlanCreateRequest
import com.data.network.dto.plan.PlanResponse
import com.data.network.dto.plan.PlanUpdateRequest
import retrofit2.Response
import retrofit2.http.*

interface PlanApi {

    @GET("plan/")
    suspend fun getPlans(): List<PlanResponse>

    @POST("plan/")
    suspend fun createPlan(@Body body: PlanCreateRequest): PlanResponse

    @PATCH("plan/{id}/")
    suspend fun updatePlan(
        @Path("id") planId: Long,
        @Body body: PlanUpdateRequest
    ): PlanResponse

    @DELETE("plan/{id}/delete/")
    suspend fun deletePlan(
        @Path("id") planId: Long
    ): Response<Unit>
}

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

    //단건등록
    @POST("plan/")
    suspend fun createPlan(@Body body: PlanCreateRequest): PlanResponse

    //여러건 등록 ex)3일치 점심 저녁
    @POST("plan/")
    suspend fun createPlanSmart(@Body body: @JvmSuppressWildcards Map<String, Any>): Response<Any>

    @PATCH("plan/{id}/")
    suspend fun updatePlan(
        @Path("id") planId: Long,
        @Body body: PlanUpdateRequest
    ): PlanResponse

    @DELETE("plan/{id}/delete/")
    suspend fun deletePlan(
        @Path("id") planId: Long
    ): Response<Unit>

    @POST("plan/{id}/taken/")
    suspend fun markAsTaken(
        @Path("id") planId: Long
    ): Response<Unit>

    @POST("plan/{id}/snooze/")
    suspend fun snoozePlan(
        @Path("id") planId: Long
    ): Response<Unit>
}

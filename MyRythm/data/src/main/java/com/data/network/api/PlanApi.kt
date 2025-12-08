package com.data.network.api

import com.data.network.dto.plan.IdResponse
import com.data.network.dto.plan.PlanCreateRequest
import com.data.network.dto.plan.PlanResponse
import com.data.network.dto.plan.PlanUpdateRequest
import retrofit2.Response
import retrofit2.http.*

interface PlanApi {

    @GET("med/plan/")
    suspend fun getPlans(): List<PlanResponse>

    @POST("med/plan/")
    suspend fun createPlan(@Body body: PlanCreateRequest): PlanResponse

    @POST("med/plan/")
    suspend fun createPlanSmart(@Body body: @JvmSuppressWildcards Map<String, Any>): Response<Any>

    @PATCH("med/plan/{id}/")
    suspend fun updatePlan(
        @Path("id") planId: Long,
        @Body body: PlanUpdateRequest
    ): PlanResponse

    @DELETE("med/plan/{id}/delete/")
    suspend fun deletePlan(
        @Path("id") planId: Long
    ): Response<Unit>

    @POST("med/plan/{id}/taken/")
    suspend fun markAsTaken(
        @Path("id") planId: Long
    ): Response<Unit>

    @POST("med/plan/{id}/snooze/")
    suspend fun snoozePlan(
        @Path("id") planId: Long
    ): Response<Unit>

    // 🔥 스태프 전용: 특정 사용자의 모든 복약 스케줄
    @GET("med/plan/user/{userId}/")
    suspend fun getUserPlans(
        @Path("userId") userId: Long
    ): List<PlanResponse>
}
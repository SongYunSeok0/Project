package com.data.network.api

import com.data.network.dto.step.StepCountRequest
import com.data.network.dto.step.DailyStepRequest
import com.data.network.dto.step.DailyStepResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface StepApi {

    // 실시간 스냅샷 업로드
    @POST("health/stepcount/")   // 전체 URL: baseUrl + "stepcount/"
    suspend fun uploadStepCount(
        @Body body: StepCountRequest
    )

    // 하루 총 걸음수 업로드
    @POST("health/dailystep/")   // 전체 URL: baseUrl + "dailystep/"
    suspend fun uploadDailyStep(
        @Body body: DailyStepRequest
    )

    @GET("/api/steps/daily/weekly/")
    suspend fun getWeeklySteps(): List<DailyStepResponse>
}

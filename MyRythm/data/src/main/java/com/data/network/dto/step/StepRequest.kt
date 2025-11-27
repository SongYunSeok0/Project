// data/src/main/java/com/data/network/dto/step/StepDtos.kt
package com.data.network.dto.step

data class StepCountRequest(
    val steps: Int,
    val collected_at: Long
)

data class DailyStepRequest(
    val date: String,
    val steps: Int
)

// 주간 조회나 일자별 조회 응답용
data class DailyStepResponse(
    val date: String,
    val steps: Int
)

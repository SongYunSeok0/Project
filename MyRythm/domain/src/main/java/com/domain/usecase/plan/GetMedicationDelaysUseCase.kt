package com.domain.usecase.plan

import com.domain.model.ApiResult
import com.domain.repository.PlanRepository
import com.domain.usecase.auth.GetCurrentUserIdUseCase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class MedicationDelayUI(
    val date: String,
    val label: String,
    val scheduledTime: Long,
    val actualTime: Long,
    val delayMinutes: Int,
    val isTaken: Boolean
)

class GetMedicationDelaysUseCase @Inject constructor(
    private val planRepository: PlanRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend operator fun invoke(): ApiResult<List<MedicationDelayUI>> {
        // GetCurrentUserIdUseCase는 Long? 반환 (ApiResult 아님)
        val userId = getCurrentUserIdUseCase()
            ?: return ApiResult.Success(emptyList())

        // PlanRepository는 ApiResult<List<Plan>> 반환
        return when (val result = planRepository.getRecentTakenPlans(userId, days = 7)) {
            is ApiResult.Success -> {
                val zone = ZoneId.systemDefault()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                val delays = result.data.mapNotNull { plan ->
                    // Plan의 필드들 모두 존재 확인됨
                    val originalTime = plan.exTakenAt ?: return@mapNotNull null
                    val actualTime = plan.takenTime ?: return@mapNotNull null
                    val label = plan.regihistoryLabel ?: return@mapNotNull null

                    val date = Instant.ofEpochMilli(originalTime)
                        .atZone(zone)
                        .toLocalDate()
                        .format(formatter)

                    val delayMinutes = ((actualTime - originalTime) / (60 * 1000)).toInt()

                    MedicationDelayUI(
                        date = date,
                        label = label,
                        scheduledTime = originalTime,
                        actualTime = actualTime,
                        delayMinutes = delayMinutes,
                        isTaken = true
                    )
                }.sortedBy { it.date }

                ApiResult.Success(delays)
            }
            is ApiResult.Failure -> ApiResult.Failure(result.error)
        }
    }
}
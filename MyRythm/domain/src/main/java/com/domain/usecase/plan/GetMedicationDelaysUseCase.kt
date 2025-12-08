package com.domain.usecase.plan

import com.domain.repository.PlanRepository
import com.domain.usecase.auth.GetCurrentUserIdUseCase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// 🔥 UI용 데이터 모델 (String date)
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
    suspend operator fun invoke(): List<MedicationDelayUI> {
        val userId = getCurrentUserIdUseCase() ?: return emptyList()

        val plans = planRepository.getRecentTakenPlans(userId, days = 7)
        val zone = ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        return plans.map { plan ->
            val originalTime = plan.exTakenAt!!
            val actualTime = plan.takenTime!!

            val date = Instant.ofEpochMilli(originalTime)
                .atZone(zone)
                .toLocalDate()
                .format(formatter)  // 🔥 String으로 변환

            val delayMinutes = ((actualTime - originalTime) / (60 * 1000)).toInt()

            MedicationDelayUI(
                date = date,
                label = plan.medName,
                scheduledTime = originalTime,
                actualTime = actualTime,
                delayMinutes = delayMinutes,
                isTaken = true
            )
        }.sortedBy { it.date }
    }
}
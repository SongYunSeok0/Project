package com.domain.usecase.plan

import com.domain.repository.PlanRepository
import com.domain.usecase.auth.GetCurrentUserIdUseCase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class MedicationDelayUI(
    val date: String,
    val label: String,           // RegiHistory의 label (예: "위염", "비타민C")
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

        return plans.mapNotNull { plan ->
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
    }
}
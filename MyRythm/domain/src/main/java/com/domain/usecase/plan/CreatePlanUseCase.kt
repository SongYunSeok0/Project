package com.domain.usecase.plan

import com.domain.repository.PlanRepository
import javax.inject.Inject

class CreatePlanUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    suspend operator fun invoke(
        prescriptionId: Long?,
        medName: String,
        takenAt: Long,
        mealTime: String?,
        note: String?,
        taken: Long?,
        useAlarm: Boolean
    ) {
        repository.create(
            prescriptionId = prescriptionId,
            medName = medName,
            takenAt = takenAt,
            mealTime = mealTime,
            note = note,
            taken = taken,
            useAlarm = useAlarm
        )
    }
}
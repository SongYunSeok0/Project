package com.domain.usecase.plan

import com.domain.model.Plan
import com.domain.repository.RegiRepository
import javax.inject.Inject

class CreatePlanUseCase @Inject constructor(
    private val repository: RegiRepository
) {
    suspend operator fun invoke(
        regihistoryId: Long?,
        medName: String,
        takenAt: Long?,
        mealTime: String?,
        note: String?,
        taken: Boolean?,
        useAlarm: Boolean
    ) {
        repository.createPlans(
            regihistoryId = regihistoryId,
            list = listOf(
                Plan(
                    id = 0L,
                    regihistoryId = regihistoryId,
                    medName = medName,
                    takenAt = takenAt,
                    mealTime = mealTime,
                    note = note,
                    taken = taken,
                    takenTime = null,
                    exTakenAt = takenAt,
                    useAlarm = useAlarm
                )
            )
        )

    }
}

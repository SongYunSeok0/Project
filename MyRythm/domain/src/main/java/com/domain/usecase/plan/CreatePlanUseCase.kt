package com.domain.usecase.plan

import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.model.Plan
import com.domain.repository.RegiRepository
import javax.inject.Inject

class CreatePlanUseCase @Inject constructor(
    private val repository: RegiRepository
) {
    suspend operator fun invoke(
        regihistoryId: Long?,
        regihistoryLabel: String?,
        medName: String,
        takenAt: Long?,
        mealTime: String?,
        note: String?,
        taken: Boolean?,
        useAlarm: Boolean
    ): ApiResult<Unit> {

        if (medName.isBlank()) {
            return ApiResult.Failure(DomainError.Validation("약 이름을 입력해주세요"))
        }

        val plan = Plan(
            id = 0L,
            regihistoryId = regihistoryId,
            regihistoryLabel = regihistoryLabel,
            medName = medName,
            takenAt = takenAt,
            mealTime = mealTime,
            note = note,
            taken = taken,
            takenTime = null,
            exTakenAt = takenAt,
            useAlarm = useAlarm
        )

        return repository.createPlans(
            regihistoryId = regihistoryId,
            list = listOf(plan)
        )
    }
}
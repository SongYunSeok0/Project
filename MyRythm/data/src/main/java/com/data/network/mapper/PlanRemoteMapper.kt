package com.data.network.mapper

import com.data.network.dto.plan.PlanResponse
import com.data.network.dto.plan.PlanUpdateRequest
import com.domain.model.Plan
import com.domain.model.PlanStatus

// Network → Domain
fun PlanResponse.toDomain(): Plan =
    Plan(
        id = id,
        regihistoryId = regihistoryId,
        medName = medName,
        takenAt = takenAt,
        exTakenAt = exTakenAt,
        mealTime = mealTime,
        note = note,
        taken = taken,
        takenTime = takenTime,
        useAlarm =  useAlarm,
        status = PlanStatus.from(status)

    )

// Domain → Network (Update)
fun Plan.toUpdateRequest(): PlanUpdateRequest = PlanUpdateRequest(
    regihistoryId = regihistoryId,
    medName = medName,
    takenAt = takenAt,
    exTakenAt = exTakenAt,
    mealTime = mealTime,
    note = note,
    taken = taken,
    useAlarm = useAlarm
)

package com.data.network.mapper

import com.data.network.dto.plan.PlanCreateRequest
import com.data.network.dto.plan.PlanResponse
import com.data.network.dto.plan.PlanUpdateRequest
import com.domain.model.Plan

// ----------------------
// Network → Domain
// ----------------------
fun PlanResponse.toDomain(): Plan =
    Plan(
        id = id,
//        userId = userId,
        regihistoryId = regihistoryId,
        medName = medName,
        takenAt = takenAt,
        mealTime = mealTime,
        note = note,
        taken = taken,
    )

// ----------------------
// Domain → Network (Update)
// ----------------------
fun Plan.toUpdateRequest(): PlanUpdateRequest = PlanUpdateRequest(
    regihistoryId = regihistoryId,
    medName = medName,
    takenAt = takenAt,
    mealTime = mealTime,
    note = note,
    taken = taken
)

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
        userId = userId,
        prescriptionId = prescriptionId,
        medName = medName,
        takenAt = takenAt,
        mealTime = mealTime,
        note = note,
        taken = taken,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

// ----------------------
// Domain → Network (Update)
// ----------------------
fun Plan.toUpdateRequest(): PlanUpdateRequest = PlanUpdateRequest(
    prescriptionId = prescriptionId,
    medName = medName,
    takenAt = takenAt,
    mealTime = mealTime,
    note = note,
    taken = taken
)

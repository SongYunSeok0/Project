package com.data.network.mapper

import com.data.network.dto.plan.PlanCreateRequest
import com.data.network.dto.plan.PlanResponse
import com.data.network.dto.plan.PlanUpdateRequest
import com.domain.model.MealRelation
import com.domain.model.Plan
import com.domain.model.PlanType

/** 서버 → Domain */
fun PlanResponse.toDomain(): Plan = Plan(
    id = id,
    type = when (type.uppercase()) {
        "DISEASE" -> PlanType.DISEASE
        else -> PlanType.SUPPLEMENT
    },
    diseaseName = diseaseName,
    supplementName = supplementName,
    dosePerDay = dosePerDay,
    mealRelation = mealRelation?.let {
        when (it.uppercase()) {
            "BEFORE" -> MealRelation.BEFORE
            "AFTER"  -> MealRelation.AFTER
            else -> MealRelation.NONE
        }
    },
    memo = memo,
    startDay = startDay,
    endDay = endDay,
    meds = meds,
    times = times,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/** Domain → 서버 등록 요청 */
fun Plan.toCreateRequest(userId: String): PlanCreateRequest = PlanCreateRequest(
    userId = userId,
    type = type.name,
    diseaseName = diseaseName,
    supplementName = supplementName,
    dosePerDay = dosePerDay,
    mealRelation = mealRelation?.name,
    memo = memo,
    startDay = startDay,
    endDay = endDay,
    meds = meds,
    times = times
)

/** Domain → 서버 수정 요청 */
fun Plan.toUpdateRequest(): PlanUpdateRequest = PlanUpdateRequest(
    type = type.name,
    diseaseName = diseaseName,
    supplementName = supplementName,
    dosePerDay = dosePerDay,
    mealRelation = mealRelation?.name,
    memo = memo,
    startDay = startDay,
    endDay = endDay,
    meds = meds,
    times = times
)

package com.data.network.mapper

import com.data.network.dto.regihistory.RegiHistoryResponse
import com.data.network.dto.regihistory.RegiHistoryWithPlansResponse
import com.domain.model.RegiHistory
import com.domain.model.RegiHistoryWithPlans

fun RegiHistoryResponse.toModel() = RegiHistory(
    id = id,
    userId = userId,
    regiType = regiType,
    label = label,
    issuedDate = issuedDate,
    useAlarm = useAlarm,
    device = device
)

// 🔥 스태프용: Plan 포함된 RegiHistory Mapper
fun RegiHistoryWithPlansResponse.toModel() = RegiHistoryWithPlans(
    id = id,
    userId = user,
    username = username,
    userEmail = userEmail,
    regiType = regiType,
    label = label,
    issuedDate = issuedDate,
    useAlarm = useAlarm,
    device = device,
    plans = plans.map { it.toDomain() },  // 🔥 PlanResponse.toDomain() 사용
    planCount = planCount
)

// 🔥 List 변환
fun List<RegiHistoryWithPlansResponse>.toModelList(): List<RegiHistoryWithPlans> {
    return map { it.toModel() }
}
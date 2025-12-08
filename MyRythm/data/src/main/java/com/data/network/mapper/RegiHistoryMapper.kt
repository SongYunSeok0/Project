package com.data.network.mapper

import com.data.network.dto.regihistory.RegiHistoryResponse
import com.domain.model.RegiHistory

fun RegiHistoryResponse.toModel() = RegiHistory(
    id = id,
    userId = userId,
    regiType = regiType,
    label = label,
    issuedDate = issuedDate,
    useAlarm = useAlarm,
    device = device
)

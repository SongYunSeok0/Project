package com.data.mapper

import com.data.db.entity.DailyStepEntity
import com.domain.model.DailyStep

fun DailyStepEntity.toDomain(): DailyStep {
    return DailyStep(
        date = this.date,
        steps = this.steps
    )
}

fun List<DailyStepEntity>.toDomainList(): List<DailyStep> {
    return this.map { it.toDomain() }
}
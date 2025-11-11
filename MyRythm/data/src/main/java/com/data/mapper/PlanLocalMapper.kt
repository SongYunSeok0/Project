package com.data.mapper

import com.data.db.entity.EMealRelation
import com.data.db.entity.EPlanType
import com.data.db.entity.PlanEntity
import com.data.db.entity.PlanWithDetails
import com.domain.model.MealRelation
import com.domain.model.Plan
import com.domain.model.PlanType
import java.util.Locale

// ---------- DB -> Domain ----------
fun PlanWithDetails.toDomain(): Plan = Plan(
    id = plan.id,
    type = when (plan.type) {
        EPlanType.DISEASE   -> PlanType.DISEASE
        EPlanType.SUPPLEMENT -> PlanType.SUPPLEMENT
    },
    diseaseName = plan.diseaseName,
    supplementName = plan.supplementName,
    dosePerDay = plan.dosePerDay,
    mealRelation = plan.mealRelation?.let {
        when (it) {
            EMealRelation.BEFORE -> MealRelation.BEFORE
            EMealRelation.AFTER  -> MealRelation.AFTER
            EMealRelation.NONE   -> MealRelation.NONE
        }
    },
    memo = plan.memo,
    startDay = plan.startDay,
    endDay = plan.endDay,
    meds = meds.map { it.name }.cleanMeds(),
    // DB는 orderIndex 기준, Domain은 문자열 리스트
    times = times.sortedBy { it.orderIndex }.map { it.hhmm }.cleanTimes(),
    createdAt = plan.createdAt,
    updatedAt = plan.updatedAt
)

// ---------- Domain -> DB(분해) ----------
fun Plan.toEntities(userId: String): Triple<PlanEntity, List<String>, List<String>> {
    val eType = when (type) {
        PlanType.DISEASE   -> EPlanType.DISEASE
        PlanType.SUPPLEMENT -> EPlanType.SUPPLEMENT
    }
    val eMeal = when (mealRelation) {
        MealRelation.BEFORE -> EMealRelation.BEFORE
        MealRelation.AFTER  -> EMealRelation.AFTER
        MealRelation.NONE   -> EMealRelation.NONE
        null -> null
    }

    val plan = PlanEntity(
        id = id,
        userId = userId,
        type = eType,
        diseaseName = diseaseName?.ifBlank { null },
        supplementName = supplementName?.ifBlank { null },
        dosePerDay = dosePerDay.coerceAtLeast(1),
        mealRelation = eMeal,
        memo = memo?.ifBlank { null },
        startDay = startDay,
        endDay = endDay,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    // Room 하위 테이블로 들어갈 원자료
    val medsForDb = meds.cleanMeds()
    val timesForDb = times.cleanTimes()

    return Triple(plan, medsForDb, timesForDb)
}

// ---------- 유틸 ----------

// 빈 값 제거 + 트림 + 중복 제거(입력 순서 유지)
private fun List<String>.cleanMeds(): List<String> =
    this.map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()

// "8:0" -> "08:00" 보정, 빈 값/이상치 제거, 중복 제거(입력 순서 유지)
private fun List<String>.cleanTimes(): List<String> =
    this.mapNotNull { normalizeHhmm(it) }
        .distinct()

/**
 * "8:0", "8:00", "08:0", " 08:00 " 등 -> "08:00"
 * 유효하지 않으면 null
 */
private fun normalizeHhmm(raw: String?): String? {
    val s = raw?.trim() ?: return null
    val parts = s.split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return String.format(Locale.US, "%02d:%02d", h, m)
}

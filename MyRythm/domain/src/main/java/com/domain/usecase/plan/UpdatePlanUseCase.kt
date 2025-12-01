package com.domain.usecase.plan

import com.domain.repository.PlanRepository
import com.domain.model.Plan
import javax.inject.Inject

class UpdatePlanUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    /**
     * planId: 수정할 약의 ID
     * newTakenAt: 변경할 시간 (Timestamp Long)
     * * 반환값(: Boolean)을 명시해야 ViewModel에서 if (success)로 쓸 수 있습니다.
     */
    suspend operator fun invoke(userId: Long, plan: Plan): Boolean {
        return try {
            repository.update(userId, plan)
            true // 성공 시 true 반환
        } catch (e: Exception) {
            e.printStackTrace()
            false // 실패 시 false 반환
        }
    }
}
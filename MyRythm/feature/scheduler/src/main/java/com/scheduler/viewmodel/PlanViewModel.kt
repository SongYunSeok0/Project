package com.scheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.Plan
import com.domain.repository.PlanRepository
import com.scheduler.ui.IntakeStatus
import com.scheduler.ui.MedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val repository: PlanRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val plans: List<Plan> = emptyList(),
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _itemsByDate = MutableStateFlow<Map<LocalDate, List<MedItem>>>(emptyMap())
    val itemsByDate: StateFlow<Map<LocalDate, List<MedItem>>> = _itemsByDate.asStateFlow()

    // ✅ Plan 목록 로드
    fun load(userId: String) {
        if (userId.isBlank()) {
            Log.e("PlanViewModel", "❌ userId가 비어있음")
            return
        }

        val uid = userId.toLongOrNull()
        if (uid == null) {
            Log.e("PlanViewModel", "❌ userId 숫자 변환 실패: $userId")
            return   // 앱 죽지 않도록 여기서 종료
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.observePlans(uid)
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { list ->
                    _uiState.update { it.copy(plans = list) }
                    _itemsByDate.value = makeItemsByDate(list)
                }
        }
    }

    // ✅ Plan 생성
    fun createPlan(
        userId: Long,
        prescriptionId: Long,
        medName: String,
        takenAt: Long,
        mealTime: String,
        note: String?,
        taken: Long?,
    ) {
        if (userId <= 0L) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(loading = true, error = null) }

                val currentMs = System.currentTimeMillis()

                Log.e(
                    "PlanViewModel",
                    """
                🔥 보내는 값 ======================
                userId = $userId
                prescriptionId = $prescriptionId
                medName = $medName
                takenAt = $takenAt
                mealTime = $mealTime
                note = $note
                taken = $taken
                createdAt = $currentMs
                updatedAt = $currentMs
                =================================
                """.trimIndent()
                )

                val plan = Plan(
                    id = 0L,              // 초기값 유지 OK
                    prescriptionId = prescriptionId,
                    medName = medName,
                    takenAt = takenAt,
                    mealTime = mealTime,
                    note = note,
                    taken = taken,
                    createdAt = currentMs,
                    updatedAt = currentMs
                )

                repository.create(userId, plan)   // 여기가 서버 전송 위치

                Log.d("PlanViewModel", "💾 Plan 생성 요청 완료: $medName")

            } catch (e: Exception) {
                Log.e("PlanViewModel", "❌ createPlan 실패", e)
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }


    fun updatePlan(userId: Long, plan: Plan) {
        if (userId <= 0L) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(loading = true, error = null) }
                repository.update(userId, plan)
                Log.d("PlanViewModel", "✏️ Plan 수정 완료: ${plan.medName}")
            } catch (e: Exception) {
                Log.e("PlanViewModel", "❌ updatePlan 실패", e)
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun deletePlan(userId: Long, planId: Long) {
        if (userId <= 0L) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(loading = true, error = null) }
                repository.delete(userId, planId)
                Log.d("PlanViewModel", "🗑️ Plan 삭제 완료: $planId")
            } catch (e: Exception) {
                Log.e("PlanViewModel", "❌ deletePlan 실패", e)
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    private fun makeItemsByDate(plans: List<Plan>): Map<LocalDate, List<MedItem>> {
        val zone = ZoneId.systemDefault()
        val out = mutableMapOf<LocalDate, MutableList<MedItem>>()

        plans.forEach { p ->
            val takenAt = p.takenAt ?: return@forEach
            val instant = Instant.ofEpochMilli(takenAt)
            val localDateTime = instant.atZone(zone)
            val localDate = localDateTime.toLocalDate()
            val localTime = localDateTime.toLocalTime().toString().substring(0, 5)

            val item = MedItem(
                name = p.medName,
                time = localTime,
                status = IntakeStatus.SCHEDULED
            )
            out.getOrPut(localDate) { mutableListOf() }.add(item)
        }

        return out.mapValues { (_, v) -> v.sortedBy { it.time } }
    }
}

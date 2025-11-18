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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
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
            return
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

    // ✅ Plan 생성 (서버에는 userId 안 보내고, 필요하면 끝에서 refresh에만 사용)
    fun createPlan(
        userId: Long,          // 로컬 refresh 용 (서버에는 안 감)
        prescriptionId: Long?,
        medName: String,
        takenAt: Long,
        mealTime: String?,
        note: String?,
        taken: Long?
    ) {
        if (userId <= 0L) {
            Log.e("PlanViewModel", "❌ createPlan: userId <= 0")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(loading = true, error = null) }

                Log.e(
                    "PlanViewModel",
                    """
                    🔥 서버로 보낼 값 =================
                    prescriptionId = $prescriptionId
                    medName        = $medName
                    takenAt        = $takenAt
                    mealTime       = $mealTime
                    note           = $note
                    taken          = $taken
                    =================================
                    """.trimIndent()
                )

                // 👉 여기서는 domain 레이어 함수만 호출
                repository.create(
                    prescriptionId = prescriptionId,
                    medName = medName,
                    takenAt = takenAt,
                    mealTime = mealTime,
                    note = note,
                    taken = taken
                )

                // 필요하면 로컬 DB 동기화
                repository.refresh(userId)

                Log.d("PlanViewModel", "💾 Plan 생성 완료: $medName")
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

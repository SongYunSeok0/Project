// feature/scheduler/src/main/java/com/scheduler/viewmodel/PlanViewModel.kt
package com.scheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.Plan
import com.domain.repository.RegiRepository
import com.scheduler.ui.IntakeStatus
import com.scheduler.ui.MedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val repository: RegiRepository,
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val plans: List<Plan> = emptyList(),
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _itemsByDate = MutableStateFlow<Map<LocalDate, List<MedItem>>>(emptyMap())
    val itemsByDate = _itemsByDate.asStateFlow()

    // 🔥 userId 기반 전체 일정 로드
    fun load(userId: Long) {
        if (userId <= 0L) {
            _uiState.update { it.copy(error = "userId 미지정") }
            return
        }

        viewModelScope.launch {
            repository.observeAllPlans(userId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { list ->
                    _uiState.update { it.copy(plans = list) }
                    _itemsByDate.value = makeItemsByDate(list)
                }
        }
    }

    // 날짜별로 묶기
    private fun makeItemsByDate(plans: List<Plan>): Map<LocalDate, List<MedItem>> {
        val zone = ZoneId.systemDefault()
        val out = mutableMapOf<LocalDate, MutableList<MedItem>>()

        plans.forEach { p ->
            val takenAt = p.takenAt ?: return@forEach
            val instant = Instant.ofEpochMilli(takenAt)
            val local = instant.atZone(zone)
            val date = local.toLocalDate()
            val time = local.toLocalTime().toString().substring(0, 5)

            val item = MedItem(
                name = p.medName,
                time = time,
                status = IntakeStatus.SCHEDULED
            )
            out.getOrPut(date) { mutableListOf() }.add(item)
        }

        return out.mapValues { (_, v) -> v.sortedBy { it.time } }
    }
}


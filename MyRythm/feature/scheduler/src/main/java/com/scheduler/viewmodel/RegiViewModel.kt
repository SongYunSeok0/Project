package com.scheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.Plan
import com.domain.repository.RegiRepository
import com.scheduler.ui.IntakeStatus
import com.scheduler.ui.MedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@HiltViewModel
class RegiViewModel @Inject constructor(
    private val repository: RegiRepository
) : ViewModel() {

    // ---------------- 이벤트(등록 완료/실패) 송신용 ----------------
    private val _events = MutableSharedFlow<String>()
    val events = _events

    data class UiState(
        val loading: Boolean = false,
        val plans: List<Plan> = emptyList(),
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _itemsByDate = MutableStateFlow<Map<LocalDate, List<MedItem>>>(emptyMap())
    val itemsByDate: StateFlow<Map<LocalDate, List<MedItem>>> = _itemsByDate.asStateFlow()

    // ---------------- Plan 목록 조회 ----------------
    fun loadPlans(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.observeAllPlans(userId)
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { list ->
                    _uiState.update { it.copy(plans = list) }
                    _itemsByDate.value = makeItemsByDate(list)
                }
        }
    }

    // ---------------- RegiHistory + Plans 생성 ----------------
    fun createRegiAndPlans(
        regiType: String,
        label: String?,
        issuedDate: String?,
        plans: List<Plan>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(loading = true, error = null) }

                // 1) RegiHistory 생성
                val regiId = repository.createRegiHistory(
                    regiType = regiType,
                    label = label,
                    issuedDate = issuedDate
                )
                Log.d("RegiViewModel", "RegiHistory 생성 완료: id=$regiId")

                // 2) Plan 생성
                repository.createPlans(regiId, plans)
                Log.d("RegiViewModel", "Plans ${plans.size}개 생성 완료")

                // 🔥 성공 이벤트
                _events.emit("등록 완료")

            } catch (e: Exception) {
                Log.e("RegiViewModel", "createRegiAndPlans 실패", e)
                _events.emit("등록 실패")
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    // ---------------- 날짜별 MedItem 변환 ----------------
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

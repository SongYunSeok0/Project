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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@HiltViewModel
class RegiViewModel @Inject constructor(
    private val repository: RegiRepository
) : ViewModel() {

    private var currentRegiHistoryId: Long? = null

    fun initRegi(regihistoryId: Long?) {
        currentRegiHistoryId = regihistoryId
        Log.d("RegiViewModel", "initRegi: regihistoryId=$regihistoryId")
    }

    private val _events = MutableSharedFlow<String>()
    val events = _events

    data class UiState(
        val loading: Boolean = false,
        val plans: List<Plan> = emptyList(),
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _itemsByDate =
        MutableStateFlow<Map<LocalDate, List<MedItem>>>(emptyMap())
    val itemsByDate: StateFlow<Map<LocalDate, List<MedItem>>> =
        _itemsByDate.asStateFlow()

    fun loadPlans(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
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

    private var isCreating = false
    fun createRegiAndPlans(
        regiType: String,
        label: String?,
        issuedDate: String?,
        useAlarm: Boolean,
        plans: List<Plan>
    ) {
        if (isCreating) return     // ← 중복 방지!!
        isCreating = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(loading = true, error = null) }

                val realRegiId = currentRegiHistoryId ?: run {
                    val newId = repository.createRegiHistory(
                        regiType = regiType,
                        label = label,
                        issuedDate = issuedDate,
                        useAlarm = useAlarm
                    )
                    newId
                }

                repository.createPlans(realRegiId, plans)

                _events.emit("등록 완료")

            } catch (e: Exception) {
                Log.e("RegiViewModel", "createRegiAndPlans 실패", e)
                _events.emit("등록 실패")
            } finally {
                _uiState.update { it.copy(loading = false) }
                isCreating = false
            }
        }
    }

    private fun makeItemsByDate(plans: List<Plan>): Map<LocalDate, List<MedItem>> {
        val zone = ZoneId.systemDefault()
        val out = mutableMapOf<LocalDate, MutableList<MedItem>>()

        // 같은 날짜, 같은 시간으로 그룹화
        plans
            .filter { it.takenAt != null }
            .groupBy { p ->
                val local = Instant.ofEpochMilli(p.takenAt!!).atZone(zone)
                val date = local.toLocalDate()
                val time = local.toLocalTime().toString().substring(0, 5)
                Pair(date, time)
            }
            .forEach { (key, group) ->
                val (date, time) = key

                // 그룹의 모든 약 이름과 ID 수집
                val medNames = group.map { it.medName }
                val planIds = group.map { it.id }

                // 대표 Plan (첫 번째)
                val representative = group.first()

                val item = MedItem(
                    planIds = planIds,
                    label = representative.medName,
                    medNames = medNames,
                    time = time,
                    mealTime = representative.mealTime,
                    memo = representative.note,
                    useAlarm = representative.useAlarm,
                    status = if (group.all { it.taken != null }) IntakeStatus.DONE else IntakeStatus.SCHEDULED
                )

                out.getOrPut(date) { mutableListOf() }.add(item)
            }

        return out.mapValues { (_, v) -> v.sortedBy { it.time } }
    }
}
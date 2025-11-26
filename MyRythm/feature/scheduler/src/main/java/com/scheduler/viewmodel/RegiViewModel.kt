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

    // ---------------------------------------------------------
    // 현재 화면에서 사용할 regihistoryId를 저장하는 변수 (핵심 추가)
    // ---------------------------------------------------------
    private var currentRegiHistoryId: Long? = null

    fun initRegi(regihistoryId: Long?) {
        currentRegiHistoryId = regihistoryId
        Log.d("RegiViewModel", "initRegi: regihistoryId=$regihistoryId")
    }

    // ---------------------------------------------------------
    // 이벤트
    // ---------------------------------------------------------
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


    // ---------------------------------------------------------
    // 전체 Plan 로딩
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // RegiHistory + Plans 생성 OR 기존 regihistoryId로 Plan만 추가
    // ---------------------------------------------------------
    fun createRegiAndPlans(
        regiType: String,
        label: String?,
        issuedDate: String?,
        useAlarm: Boolean,
        plans: List<Plan>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(loading = true, error = null) }

                // ---------------------------------------------------------
                // 핵심 수정: 기존 regiHistory가 있으면 그대로 사용
                // ---------------------------------------------------------
                val realRegiId = currentRegiHistoryId ?: run {
                    val newId = repository.createRegiHistory(
                        regiType = regiType,
                        label = label,
                        issuedDate = issuedDate,
                        useAlarm = useAlarm
                    )
                    Log.d("RegiViewModel", "새 RegiHistory 생성됨: $newId")
                    newId
                }

                Log.d("RegiViewModel", "최종 사용 regiHistoryId: $realRegiId")

                // ---------------------------------------------------------
                // Plans 생성 (regihistoryId 설정 필요)
                // ---------------------------------------------------------
                repository.createPlans(realRegiId, plans)
                Log.d("RegiViewModel", "Plans ${plans.size}개 생성 완료")

                _events.emit("등록 완료")

            } catch (e: Exception) {
                Log.e("RegiViewModel", "createRegiAndPlans 실패", e)
                _events.emit("등록 실패")
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    // ---------------------------------------------------------
    // 날짜별 아이템 정렬
    // ---------------------------------------------------------
    private fun makeItemsByDate(plans: List<Plan>): Map<LocalDate, List<MedItem>> {
        val zone = ZoneId.systemDefault()
        val out = mutableMapOf<LocalDate, MutableList<MedItem>>()

        plans.forEach { p ->
            val takenAt = p.takenAt ?: return@forEach
            val local = Instant.ofEpochMilli(takenAt).atZone(zone)
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

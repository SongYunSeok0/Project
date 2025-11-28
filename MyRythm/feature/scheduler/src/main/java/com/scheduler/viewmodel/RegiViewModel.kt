package com.scheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.Plan
import com.domain.repository.PlanRepository
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
    private val regiRepository: RegiRepository,
    private val planRepository: PlanRepository,
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
            regiRepository.observeAllPlans(userId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { list ->
                    _uiState.update { it.copy(plans = list) }
                    _itemsByDate.value = makeItemsByDate(list)
                }
        }
    }

    // ✅ [수정] 여러 약(List<String>)을 한 번에 등록하도록 변경
    fun createRegiAndSmartPlans(
        regiType: String,
        label: String?,
        issuedDate: String?,
        useAlarm: Boolean,
        startDate: String,
        duration: Int,
        times: List<String>,
        medNames: List<String> // 👈 String -> List<String> 변경
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(loading = true, error = null) }


                // 1. 처방전(RegiHistory)은 1개만 생성 (약들이 그룹으로 묶임)
                val realRegiId = currentRegiHistoryId ?: run {
                    regiRepository.createRegiHistory(
                        regiType = regiType,
                        label = label, // 병명(감기 등)
                        issuedDate = issuedDate,
                        useAlarm = useAlarm
                    )
                }

                // 2. 각 약 이름별로 스마트 플랜 생성 요청 (반복문)
                medNames.forEach { medName ->
                    planRepository.createPlansSmart(
                        regihistoryId = realRegiId,
                        startDate = startDate,
                        duration = duration,
                        times = times,
                        medName = medName // 각각의 약 이름(A, B...) 전달
                    )
                }

                _events.emit("등록 완료")

            } catch (e: Exception) {
                Log.e("RegiViewModel", "createRegiAndSmartPlans 실패", e)
                _events.emit("등록 실패")
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
            val local = Instant.ofEpochMilli(takenAt).atZone(zone)
            val date = local.toLocalDate()
            val time = local.toLocalTime().toString().substring(0, 5)

            val item = MedItem(
                label = p.medName,
                time = time,
                status = IntakeStatus.SCHEDULED
            )
            out.getOrPut(date) { mutableListOf() }.add(item)
        }

        return out.mapValues { (_, v) -> v.sortedBy { it.time } }
    }
}
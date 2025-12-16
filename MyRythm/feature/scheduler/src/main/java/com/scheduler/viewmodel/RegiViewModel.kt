package com.scheduler.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.Device
import com.domain.model.Plan
import com.domain.usecase.device.GetMyDevicesUseCase
import com.domain.usecase.plan.CreatePlanUseCase
import com.domain.usecase.plan.GetPlanUseCase
import com.domain.usecase.regi.CreateRegiHistoryUseCase
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
    // ✅ 전부 usecase 로만 의존
    private val getPlanUseCase: GetPlanUseCase,
    private val createPlanUseCase: CreatePlanUseCase,
    private val createRegiHistoryUseCase: CreateRegiHistoryUseCase,
    private val getMyDevicesUseCase: GetMyDevicesUseCase,
) : ViewModel() {

    private var currentRegiHistoryId: Long? = null

    fun initRegi(regihistoryId: Long?) {
        currentRegiHistoryId = regihistoryId
        Log.d("RegiViewModel", "initRegi: regihistoryId=$regihistoryId")
    }

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events

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

    // 기기 목록 (도메인 Device 그대로 사용)
    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices.asStateFlow()

    /** 전체 플랜 스트림 – GetPlanUseCase 사용 */
    fun loadPlans(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            getPlanUseCase(userId)              // Flow<List<Plan>> 라고 가정
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { list ->
                    _uiState.update { it.copy(plans = list) }
                    _itemsByDate.value = makeItemsByDate(list)
                }
        }
    }

    /** 내 IoT 기기 목록 – GetMyDevicesUseCase 사용 */
    fun loadMyDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getMyDevicesUseCase() }
                .onSuccess { list ->
                    _devices.value = list
                }
                .onFailure { e ->
                    Log.e("RegiViewModel", "loadMyDevices 실패", e)
                }
        }
    }

    private var isCreating = false

    /** RegiHistory + Plan 생성 – CreateRegiHistoryUseCase + CreatePlanUseCase 사용 */
    fun createRegiAndPlans(
        regiType: String,
        label: String?,
        issuedDate: String?,
        useAlarm: Boolean,
        plans: List<Plan>,
        device: Long?
    ) {
        if (isCreating) return
        isCreating = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(loading = true, error = null) }

                // 1) RegiHistory 생성 (없을 때만)
                val realRegiId = currentRegiHistoryId ?: run {
                    createRegiHistoryUseCase(
                        regiType = regiType,
                        label = label,
                        issuedDate = issuedDate,
                        useAlarm = useAlarm,
                        device = device
                    )
                }

                // 2) 각 플랜 생성 – CreatePlanUseCase 파라미터에 맞춰 하나씩 호출
                plans.forEach { plan ->
                    createPlanUseCase(
                        regihistoryId = realRegiId,
                        regihistoryLabel = label,
                        medName = plan.medName,
                        takenAt = plan.takenAt,
                        mealTime = plan.mealTime,
                        note = plan.note,
                        taken = plan.taken,
                        useAlarm = plan.useAlarm
                    )
                }

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

    /** 날짜별 MedItem 묶기 (예전 로직 그대로) */
    private fun makeItemsByDate(plans: List<Plan>): Map<LocalDate, List<MedItem>> {
        val zone = ZoneId.systemDefault()
        val out = mutableMapOf<LocalDate, MutableList<MedItem>>()

        plans
            .filter { it.takenAt != null }
            .groupBy { p ->
                val local = Instant.ofEpochMilli(p.takenAt!!).atZone(zone)
                val date = local.toLocalDate()
                val time = local.toLocalTime().toString().substring(0, 5)
                date to time
            }
            .forEach { (key, group) ->
                val (date, time) = key
                val medNames = group.map { it.medName }
                val planIds = group.map { it.id }
                val representative = group.first()

                val item = MedItem(
                    planIds = planIds,
                    label = representative.medName,
                    medNames = medNames,
                    time = time,
                    mealTime = representative.mealTime,
                    memo = representative.note,
                    useAlarm = representative.useAlarm,
                    status = if (group.all { it.taken != null }) {
                        IntakeStatus.DONE
                    } else {
                        IntakeStatus.SCHEDULED
                    }
                )

                out.getOrPut(date) { mutableListOf() }.add(item)
            }

        return out.mapValues { (_, v) -> v.sortedBy { it.time } }
    }
}

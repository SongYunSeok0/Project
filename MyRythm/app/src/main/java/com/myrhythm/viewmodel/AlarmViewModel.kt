package com.myrhythm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.ApiResult
import com.domain.usecase.plan.GetPlanByIdUseCase
import com.domain.usecase.plan.MarkMedTakenUseCase
import com.domain.usecase.plan.SnoozeMedUseCase
import com.domain.usecase.user.GetUserUseCase
import com.domain.usecase.regi.GetRegiHistoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val markMedTakenUseCase: MarkMedTakenUseCase,
    private val snoozeMedUseCase: SnoozeMedUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getPlanByIdUseCase: GetPlanByIdUseCase,
    private val getRegiHistoriesUseCase: GetRegiHistoriesUseCase
) : ViewModel() {

    private val tag = "AlarmViewModel"

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    sealed class AlarmEvent {
        object Success : AlarmEvent()
        data class Error(val msg: String) : AlarmEvent()
    }

    private val _eventChannel = Channel<AlarmEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    fun loadData(planId: Long) {
        viewModelScope.launch {
            try {
                Log.e(tag, "========================================")
                Log.e(tag, "데이터 로드 시작 - planId: $planId")

                var regihistoryId: Long? = null
                var userId: Long? = null

                // 1단계: Plan 조회
                getPlanByIdUseCase(planId)
                    .take(1)
                    .collect { plan ->
                        if (plan != null) {
                            Log.e(tag, "Plan 찾음")
                            Log.e(tag, "  - plan.id: ${plan.id}")
                            Log.e(tag, "  - plan.regihistoryId: ${plan.regihistoryId}")
                            Log.e(tag, "  - plan.medName: ${plan.medName}")
                            Log.e(tag, "  - plan.takenAt: ${plan.takenAt}")
                            Log.e(tag, "  - plan.mealTime: ${plan.mealTime}")
                            Log.e(tag, "  - plan.note: ${plan.note}")

                            regihistoryId = plan.regihistoryId

                            _uiState.update {
                                it.copy(
                                    medicineLabel = plan.medName,
                                    takenAtTime = formatTime(plan.takenAt),
                                    mealTime = plan.mealTime ?: "",
                                    note = plan.note ?: ""
                                )
                            }

                            Log.e(tag, "Plan 정보 업데이트 완료")
                        } else {
                            Log.e(tag, "Plan을 찾을 수 없음 - planId: $planId")
                        }
                    }

                // 2단계: RegiHistory 조회
                if (regihistoryId != null) {
                    Log.e(tag, "RegiHistory 조회 시작 - regihistoryId: $regihistoryId")

                    getRegiHistoriesUseCase()
                        .take(1)
                        .collect { histories ->
                            Log.e(tag, "전체 RegiHistory 개수: ${histories.size}")

                            val history = histories.find { it.id == regihistoryId }

                            if (history != null) {
                                Log.e(tag, "RegiHistory 찾음")
                                Log.e(tag, "  - history.id: ${history.id}")
                                Log.e(tag, "  - history.userId: ${history.userId}")
                                Log.e(tag, "  - history.label: ${history.label}")
                                Log.e(tag, "  - history.device: ${history.device}")

                                userId = history.userId

                                val isOwnDevice = history.device == null

                                val label = history.label
                                if (!label.isNullOrBlank()) {
                                    _uiState.update {
                                        it.copy(
                                            medicineLabel = label,
                                            isOwnDevice = isOwnDevice
                                        )
                                    }
                                    Log.e(tag, "label로 medicineLabel 업데이트")
                                } else {
                                    _uiState.update {
                                        it.copy(isOwnDevice = isOwnDevice)
                                    }
                                }

                                Log.e(tag, "isOwnDevice: $isOwnDevice (device null이면 true)")
                                Log.e(tag, "RegiHistory 정보 업데이트 완료")
                            } else {
                                Log.e(tag, "RegiHistory를 찾을 수 없음 - regihistoryId: $regihistoryId")
                                Log.e(tag, "존재하는 RegiHistory IDs: ${histories.map { it.id }}")
                            }
                        }
                } else {
                    Log.w(tag, "regihistoryId가 null입니다")
                }

                // 3단계: User 조회
                if (userId != null) {
                    Log.e(tag, "User 조회 시작 - userId: $userId")

                    val user = getUserUseCase(userId.toString())

                    Log.e(tag, "User 찾음")
                    Log.e(tag, "  - user.id: ${user.id}")
                    Log.e(tag, "  - user.username: ${user.username}")
                    Log.e(tag, "  - user.phoneNumber: ${user.phone}") // 필드명 맞게 조정

                    _uiState.update {
                        it.copy(
                            username = user.username ?: "사용자",
                            phoneNumber = user.phone ?: ""   // ← 전화번호 저장
                        )
                    }

                    Log.e(tag, "User 정보 업데이트 완료")
                } else {
                    Log.w(tag, "userId가 null입니다")
                }

                Log.e(tag, "최종 UI 상태:")
                Log.e(tag, "  - username: '${_uiState.value.username}'")
                Log.e(tag, "  - medicineLabel: '${_uiState.value.medicineLabel}'")
                Log.e(tag, "  - takenAtTime: '${_uiState.value.takenAtTime}'")
                Log.e(tag, "  - mealTime: '${_uiState.value.mealTime}'")
                Log.e(tag, "  - note: '${_uiState.value.note}'")
                Log.e(tag, "  - phoneNumber: '${_uiState.value.phoneNumber}'")
                Log.e(tag, "  - isOwnDevice: ${_uiState.value.isOwnDevice}")
                Log.e(tag, "========================================")

            } catch (e: Exception) {
                Log.e(tag, "데이터 로드 실패", e)
                Log.e(tag, "========================================")
                e.printStackTrace()
                _eventChannel.send(AlarmEvent.Error("데이터 로드에 실패했습니다."))
            }
        }
    }

    private fun formatTime(timestamp: Long?): String {
        if (timestamp == null) {
            Log.w(tag, "timestamp가 null입니다")
            return ""
        }
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formatted = sdf.format(Date(timestamp))
            Log.d(tag, "시간 변환: $timestamp -> $formatted")
            formatted
        } catch (e: Exception) {
            Log.e(tag, "시간 변환 실패", e)
            ""
        }
    }

    fun markAsTaken(planId: Long) = viewModelScope.launch {
        when (val result = markMedTakenUseCase(planId)) {
            is ApiResult.Success -> {
                Log.i(tag, "복약 완료 성공")
                _eventChannel.send(AlarmEvent.Success)
            }
            is ApiResult.Failure -> {
                Log.e(tag, "복약 완료 실패: ${result.error}")
                _eventChannel.send(
                    AlarmEvent.Error("복약 처리에 실패했습니다.")
                )
            }
        }
    }


    fun snooze(planId: Long) = viewModelScope.launch {
        when (val result = snoozeMedUseCase(planId)) {
            is ApiResult.Success -> {
                Log.i(tag, "미루기 성공")
                _eventChannel.send(AlarmEvent.Success)
            }
            is ApiResult.Failure -> {
                Log.e(tag, "미루기 실패: ${result.error}")
                _eventChannel.send(
                    AlarmEvent.Error("미루기에 실패했습니다.")
                )
            }
        }
    }

}

data class AlarmUiState(
    val username: String = "",
    val medicineLabel: String = "",
    val takenAtTime: String = "",
    val mealTime: String = "",
    val note: String = "",
    val isOwnDevice: Boolean = true,
    val phoneNumber: String = ""
)

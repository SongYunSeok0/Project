package com.myrhythm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.usecase.plan.MarkMedTakenUseCase
import com.domain.usecase.plan.SnoozeMedUseCase
import com.domain.usecase.user.GetUserUseCase
import com.domain.usecase.plan.GetPlansUseCase
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
    private val getPlansUseCase: GetPlansUseCase,
    private val getRegiHistoriesUseCase: GetRegiHistoriesUseCase
) : ViewModel() {

    // UI 상태
    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    // 이벤트
    sealed class AlarmEvent {
        object Success : AlarmEvent()
        data class Error(val msg: String) : AlarmEvent()
    }

    private val _eventChannel = Channel<AlarmEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    // 데이터 로드
    fun loadData(userId: String, planId: Long, regiId: Long) {
        viewModelScope.launch {
            try {
                // 사용자 정보
                val user = getUserUseCase(userId)
                _uiState.update { it.copy(username = user.username ?: "사용자") }

                // Plan 정보 (takenAt, mealTime, note만 가져옴)
                launch {
                    getPlansUseCase(user.id)
                        .collect { plans ->
                            val plan = plans.find { it.id == planId }
                            plan?.let { p ->
                                _uiState.update {
                                    it.copy(
                                        takenAtTime = formatTime(p.takenAt),
                                        mealTime = p.mealTime ?: "",
                                        note = p.note ?: ""
                                    )
                                }
                            }
                        }
                }

                launch {
                    getRegiHistoriesUseCase()
                        .collect { histories ->
                            val history = histories.find { it.id == regiId }
                            history?.let { h ->
                                _uiState.update { it.copy(medicineLabel = h.label!!) }
                            }
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // timestamp를 시간 문자열로 변환 (HH:mm 형식)
    private fun formatTime(timestamp: Long?): String {
        if (timestamp == null) return ""
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    // 복약 완료
    fun markAsTaken(planId: Long) = viewModelScope.launch {
        markMedTakenUseCase(planId)
            .onSuccess {
                _eventChannel.send(AlarmEvent.Success)
            }
            .onFailure {
                _eventChannel.send(AlarmEvent.Error("복약 처리에 실패했습니다."))
            }
    }

    // 미루기 (30분)
    fun snooze(planId: Long) = viewModelScope.launch {
        snoozeMedUseCase(planId)
            .onSuccess {
                _eventChannel.send(AlarmEvent.Success)
            }
            .onFailure {
                _eventChannel.send(AlarmEvent.Error("미루기에 실패했습니다."))
            }
    }
}

data class AlarmUiState(
    val username: String = "",
    val medicineLabel: String = "",
    val takenAtTime: String = "",  // 복용 시간 (HH:mm)
    val mealTime: String = "",     // 식사 시간
    val note: String = ""          // 메모
)
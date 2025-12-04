package com.myrhythm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.usecase.plan.MarkMedTakenUseCase
import com.domain.usecase.plan.SnoozeMedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val markMedTakenUseCase: MarkMedTakenUseCase,
    private val snoozeMedUseCase: SnoozeMedUseCase
) : ViewModel() {

    // 화면(Activity)에 보낼 이벤트 정의
    sealed class AlarmEvent {
        object Success : AlarmEvent()          // 성공 -> 화면 끄기
        data class Error(val msg: String) : AlarmEvent() // 실패 -> 토스트 메시지
    }

    private val _eventChannel = Channel<AlarmEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    // 1. 복약 완료
    fun markAsTaken(planId: Long) = viewModelScope.launch {
        markMedTakenUseCase(planId)
            .onSuccess {
                _eventChannel.send(AlarmEvent.Success)
            }
            .onFailure {
                _eventChannel.send(AlarmEvent.Error("복약 처리에 실패했습니다."))
            }
    }

    // 2. 미루기 (30분)
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
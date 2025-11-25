package com.myrhythm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.core.auth.JwtUtils
import com.data.core.auth.TokenStore
import com.domain.model.Plan
import com.domain.usecase.plan.GetPlansUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getPlansUseCase: GetPlansUseCase,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _nextTime = MutableStateFlow<String?>(null)
    val nextTime = _nextTime

    private val _remainText = MutableStateFlow<String?>(null)
    val remainText = _remainText

    init {
        val access = tokenStore.current().access
        val userIdStr = JwtUtils.extractUserId(access)

        val userId = userIdStr?.toLongOrNull()
        if (userId != null && userId > 0) {
            observePlans(userId)
        }
    }

    private fun observePlans(userId: Long) {
        getPlansUseCase(userId)
            .onEach { plans -> updateNextPlan(plans) }
            .launchIn(viewModelScope)
    }

    private fun updateNextPlan(plans: List<Plan>) {
        val now = System.currentTimeMillis()

        val next = plans
            .filter { it.takenAt != null && it.takenAt!! >= now }
            .minByOrNull { it.takenAt!! }

        if (next != null) {
            val nextAt = next.takenAt!!

            // 🔹 다음 복용 시간 포맷
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            _nextTime.value = formatter.format(Date(nextAt))

            // 🔹 남은 시간 계산 → HH:mm 으로 바꾸기
            val diff = nextAt - now
            val totalMinutes = diff / 1000 / 60

            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60

            _remainText.value = String.format("%02d:%02d", hours, minutes)
        } else {
            _nextTime.value = null
            _remainText.value = "복용 일정 없음"
        }
    }

}



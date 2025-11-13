package com.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.repository.InquiryRepository
import com.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val repository: InquiryRepository
) : ViewModel() {

    private val _events = Channel<String>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onLogout() = viewModelScope.launch {
        runCatching { logoutUseCase() }
            .onSuccess { _events.send("logout_ok") }
            .onFailure { _events.send("logout_fail") }
    }

    val inquiries = repository.getInquiries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addInquiry(type: String, title: String, content: String) {
        viewModelScope.launch {
            repository.addInquiry(type, title, content)
        }
    }

}

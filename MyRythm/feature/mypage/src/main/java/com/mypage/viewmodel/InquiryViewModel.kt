package com.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.usecase.inquiry.GetInquiriesUseCase
import com.domain.usecase.inquiry.AddInquiryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class InquiryViewModel @Inject constructor(
    private val getInquiriesUseCase: GetInquiriesUseCase,
    private val addInquiryUseCase: AddInquiryUseCase
) : ViewModel() {

    private val _events = Channel<MyPageEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val inquiries = getInquiriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addInquiry(type: String, title: String, content: String) {
        viewModelScope.launch {
            Log.e("InquiryViewModel", "📝 문의 등록 시작")
            runCatching { addInquiryUseCase(type, title, content) }
                .onSuccess {
                    Log.e("InquiryViewModel", "✅ 문의 등록 성공")
                    _events.send(MyPageEvent.InquirySubmitSuccess)
                }
                .onFailure { e ->
                    Log.e("InquiryViewModel", "❌ 문의 등록 실패: ${e.message}", e)
                    _events.send(
                        MyPageEvent.InquirySubmitFailed(
                            e.message ?: "문의 등록 실패"
                        )
                    )
                }
        }
    }
}

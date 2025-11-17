package com.chatbot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.usecase.chatbot.AskChatbotUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatbotViewModel(
    private val askChatbot: AskChatbotUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val question: String = "",
        val answer: String = "",
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun onQuestionChange(q: String) {
        _state.update { it.copy(question = q) }
    }

    fun send() {
        val q = state.value.question.trim()
        if (q.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            runCatching {
                askChatbot(q)             // ← ChatAnswer 리턴
            }.onSuccess { ans ->
                _state.update {
                    it.copy(
                        loading = false,
                        answer = ans.answer   // ✅ ChatAnswer.answer 만 저장
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        loading = false,
                        error = e.message ?: "알 수 없는 오류"
                    )
                }
            }
        }
    }
}

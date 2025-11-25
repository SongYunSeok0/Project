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

    data class Message(
        val id: Long,
        val isUser: Boolean,
        val text: String
    )

    data class UiState(
        val loading: Boolean = false,
        val input: String = "",
        val messages: List<Message> = emptyList(),
        val error: String? = null,
        val lastMedName: String? = null
    )

    private val _state = MutableStateFlow(UiState())

    val state: StateFlow<UiState> = _state

    fun onQuestionChange(q: String) {
        _state.update { it.copy(input = q) }
    }

    fun send() {
        val raw = state.value.input.trim()
        if (raw.isEmpty()) return

        val currentMed = extractMedName(raw)
        val newLastMed = currentMed ?: state.value.lastMedName
        val effectiveQuestion = when {
            currentMed != null -> raw
            newLastMed != null -> "$newLastMed $raw"
            else -> raw
        }

        val userMsg = Message(
            id = System.currentTimeMillis(),
            isUser = true,
            text = raw
        )

        _state.update {
            it.copy(
                input = "",
                loading = true,
                error = null,
                lastMedName = newLastMed,
                messages = it.messages + userMsg
            )
        }

        viewModelScope.launch {
            runCatching {
                askChatbot(effectiveQuestion)
            }.onSuccess { ans ->
                val botMsg = Message(
                    id = System.currentTimeMillis(),
                    isUser = false,
                    text = ans.answer
                )
                _state.update {
                    it.copy(
                        loading = false,
                        messages = it.messages + botMsg
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
    private fun extractMedName(text: String): String? {
        val pattern = Regex("([가-힣A-Za-z0-9]+(?:정제|정|캡슐|연질캡슐|시럽|액|현탁액|산|콜드|펜))")
        val match = pattern.find(text.replace(" ", ""))
        return match?.groupValues?.getOrNull(1)
    }

    fun clearAllMessages() {
        _state.update {
            it.copy(
                messages = emptyList(),
                input = "",
                error = null,
                loading = false
            )
        }
    }
}

package com.chatbot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatbot.presentation.model.ChatEvent
import com.chatbot.presentation.model.ChatMessage
import com.domain.usecase.chatbot.AskChatbotUseCase
import com.domain.usecase.chatbot.BuildChatContextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val askChatbot: AskChatbotUseCase,
    private val buildChatContext: BuildChatContextUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val input: String = "",
        val messages: List<ChatMessage> = emptyList(),
        val error: String? = null,
        val lastMedName: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.InputChanged -> handleInputChanged(event.text)
            is ChatEvent.SendMessage -> sendMessage()
            is ChatEvent.ClearMessages -> clearAllMessages()
        }
    }

    private fun handleInputChanged(text: String) {
        _state.update { it.copy(input = text) }
    }

    private fun sendMessage() {
        val raw = state.value.input.trim()
        if (raw.isEmpty()) return

        val queryContext = buildChatContext(raw, state.value.lastMedName)

        val userMsg = ChatMessage(
            id = System.currentTimeMillis(),
            isUser = true,
            text = raw
        )

        _state.update {
            it.copy(
                input = "",
                loading = true,
                error = null,
                lastMedName = queryContext.medicationName,
                messages = it.messages + userMsg
            )
        }

        viewModelScope.launch {
            runCatching {
                askChatbot(queryContext.effectiveQuery)
            }.onSuccess { ans ->
                val botMsg = ChatMessage(
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

    private fun clearAllMessages() {
        _state.update {
            it.copy(
                messages = emptyList(),
                input = "",
                error = null,
                loading = false,
                lastMedName = null
            )
        }
    }
}
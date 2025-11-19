package com.chatbot.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chatbot.di.ChatbotEntryPoint
import dagger.hilt.android.EntryPointAccessors

class ChatbotViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ChatbotEntryPoint::class.java
        )

        val askUseCase = entryPoint.askChatbotUseCase()

        @Suppress("UNCHECKED_CAST")
        return ChatbotViewModel(
            askChatbot = askUseCase
        ) as T
    }
}

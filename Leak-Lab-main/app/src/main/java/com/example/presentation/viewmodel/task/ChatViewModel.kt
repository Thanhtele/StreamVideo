package com.example.presentation.viewmodel.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.MessageEntity
import com.example.data.di.ServiceLocator
import com.example.data.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val messageRepository = ServiceLocator.messageRepository

    private val _messagesState = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messagesState = _messagesState.asStateFlow()

    init {
        // Register direct callback events to message triggers
        MessageRepository.messageCallbacks.add { msg ->
            viewModelScope.launch {
                messageRepository?.sendMessage("Bot Response", "Acknowledged: ${msg.messageText}", false)
            }
        }

        viewModelScope.launch {
            messageRepository?.messages?.collect {
                _messagesState.value = it
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            messageRepository?.sendMessage("User", text, true)
        }
    }
}
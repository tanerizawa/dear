// di app/src/main/java/com/psy/dear/presentation/chat/ChatViewModel.kt

package com.psy.dear.presentation.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.dear.core.Result
import com.psy.dear.domain.use_case.chat.ChatUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatUseCases: ChatUseCases
) : ViewModel() {

    var uiState by mutableStateOf(ChatUiState())
        private set

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.OnMessageChange -> {
                uiState = uiState.copy(currentMessage = event.message)
            }
            is ChatEvent.SendMessage -> {
                sendMessage()
            }
        }
    }

    private fun sendMessage() {
        val messageToSend = uiState.currentMessage
        if (messageToSend.isBlank()) return

        // Update UI state to show loading
        uiState = uiState.copy(
            isSending = true,
            currentMessage = "" // Clear input field
        )

        viewModelScope.launch {
            try {
                // Optimistically add user message to the list
                // val optimisticUserMessage = ...
                // uiState = uiState.copy(messages = uiState.messages + optimisticUserMessage)

                when (val result = chatUseCases.sendMessage(messageToSend)) {
                    is Result.Success -> {
                        // TODO: Refresh chat history from a single source of truth (e.g., local db)
                        // For now, let's assume we need to refetch or the response gives us what we need
                    }
                    is Result.Error -> {
                        uiState = uiState.copy(error = result.message ?: "An unknown error occurred")
                    }
                }
            } finally {
                // Stop loading state regardless of outcome
                uiState = uiState.copy(isSending = false)
                // TODO: Here you would ideally trigger a full refresh of the chat history
                // from the local database which has been updated by the server response.
            }
        }
    }
}

data class ChatUiState(
    val messages: List<Any> = emptyList(), // Use a proper domain model
    val currentMessage: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)

sealed class ChatEvent {
    data class OnMessageChange(val message: String) : ChatEvent()
    object SendMessage : ChatEvent()
}
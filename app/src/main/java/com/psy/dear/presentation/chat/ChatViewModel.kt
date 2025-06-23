// di app/src/main/java/com/psy/dear/presentation/chat/ChatViewModel.kt

package com.psy.dear.presentation.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.dear.core.Result
import com.psy.dear.core.UnauthorizedException
import com.psy.dear.domain.use_case.chat.GetChatHistoryUseCase
import com.psy.dear.domain.use_case.chat.SendMessageUseCase
import com.psy.dear.domain.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getChatHistoryUseCase: GetChatHistoryUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    var uiState by mutableStateOf(ChatUiState())
        private set

    private val _eventFlow = MutableSharedFlow<ChatUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        getChatHistoryUseCase()
            .onEach { messages ->
                uiState = uiState.copy(messages = messages)
            }
            .launchIn(viewModelScope)
    }

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

                when (val result = sendMessageUseCase(messageToSend)) {
                    is Result.Success -> {
                        // No additional action needed here, messages will be refreshed below
                    }
                    is Result.Error -> {
                        if (result.exception is UnauthorizedException) {
                            _eventFlow.emit(ChatUiEvent.NavigateToLogin)
                        }
                        uiState = uiState.copy(error = result.exception?.message ?: "An unknown error occurred")
                    }
                    is Result.Loading -> {
                        // No-op: loading state is handled outside this scope
                    }
                }
            } finally {
                // Stop loading state regardless of outcome and refresh chat history
                uiState = uiState.copy(
                    isSending = false,
                    messages = getChatHistoryUseCase().first()
                )
            }
        }
    }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentMessage: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)

sealed class ChatEvent {
    data class OnMessageChange(val message: String) : ChatEvent()
    object SendMessage : ChatEvent()
}

sealed class ChatUiEvent {
    object NavigateToLogin : ChatUiEvent()
}
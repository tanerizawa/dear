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
import com.psy.dear.domain.use_case.chat.DeleteMessageUseCase
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
    private val sendMessageUseCase: SendMessageUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase
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
            is ChatEvent.EnterSelection -> {
                enterSelection(event.id)
            }
            is ChatEvent.ToggleSelection -> {
                toggleSelection(event.id)
            }
            is ChatEvent.DeleteSelected -> {
                deleteSelected()
            }
            is ChatEvent.ExitSelection -> {
                exitSelection()
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

    private fun enterSelection(id: String) {
        uiState = uiState.copy(
            selectionMode = true,
            selectedIds = setOf(id)
        )
    }

    private fun toggleSelection(id: String) {
        val current = uiState.selectedIds.toMutableSet()
        if (!current.add(id)) current.remove(id)
        uiState = uiState.copy(
            selectedIds = current,
            selectionMode = current.isNotEmpty()
        )
    }

    private fun deleteSelected() {
        viewModelScope.launch {
            val ids = uiState.selectedIds
            ids.forEach { deleteMessageUseCase(it) }
            uiState = uiState.copy(
                messages = getChatHistoryUseCase().first(),
                selectionMode = false,
                selectedIds = emptySet()
            )
        }
    }

    private fun exitSelection() {
        uiState = uiState.copy(selectionMode = false, selectedIds = emptySet())
    }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentMessage: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
    val selectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet()
)

sealed class ChatEvent {
    data class OnMessageChange(val message: String) : ChatEvent()
    object SendMessage : ChatEvent()
    data class EnterSelection(val id: String) : ChatEvent()
    data class ToggleSelection(val id: String) : ChatEvent()
    object DeleteSelected : ChatEvent()
    object ExitSelection : ChatEvent()
}

sealed class ChatUiEvent {
    object NavigateToLogin : ChatUiEvent()
}
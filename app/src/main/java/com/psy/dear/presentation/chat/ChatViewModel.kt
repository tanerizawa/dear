package com.psy.dear.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.dear.core.Result
import com.psy.dear.domain.model.ChatMessage
import com.psy.dear.domain.use_case.chat.GetChatHistoryUseCase
import com.psy.dear.domain.use_case.chat.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    getChatHistoryUseCase: GetChatHistoryUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state = _state.asStateFlow()

    init {
        getChatHistoryUseCase()
            .onEach { messages ->
                _state.update { it.copy(messages = messages) }
            }
            .launchIn(viewModelScope)
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = sendMessageUseCase(message)) {
                is Result.Success -> _state.update { it.copy(isLoading = false) }
                is Result.Error -> _state.update {
                    it.copy(isLoading = false, error = result.exception?.message ?: "Gagal mengirim pesan")
                }
                else -> {}
            }
        }
    }
}

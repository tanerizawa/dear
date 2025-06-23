package com.psy.dear.domain.use_case.chat

import com.psy.dear.core.Result
import com.psy.dear.domain.model.ChatMessage
import com.psy.dear.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChatHistoryUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(): Flow<List<ChatMessage>> = repository.getChatHistory()
}

class SendMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(message: String): Result<Unit> {
        if (message.isBlank()) {
            return Result.Error(IllegalArgumentException("Pesan tidak boleh kosong."))
        }
        return repository.sendMessage(message)
    }
}

class DeleteMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.deleteMessage(id)
}

class FlagMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(id: String, flagged: Boolean): Result<Unit> =
        repository.flagMessage(id, flagged)
}

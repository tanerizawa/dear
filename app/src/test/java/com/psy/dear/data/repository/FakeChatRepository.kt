package com.psy.dear.data.repository

import com.psy.dear.core.Result
import com.psy.dear.domain.model.ChatMessage
import com.psy.dear.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeChatRepository : ChatRepository {
    private val messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    var sendResult: Result<Unit> = Result.Success(Unit)
    val deletedIds = mutableListOf<String>()

    override fun getChatHistory(): Flow<List<ChatMessage>> = messagesFlow.asStateFlow()

    override suspend fun sendMessage(message: String): Result<Unit> {
        return sendResult
    }

    override suspend fun deleteMessage(id: String): Result<Unit> {
        deletedIds.add(id)
        return Result.Success(Unit)
    }

    override suspend fun flagMessage(id: String, flagged: Boolean): Result<Unit> = Result.Success(Unit)
}

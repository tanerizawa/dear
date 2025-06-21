package com.psy.dear.domain.repository

import com.psy.dear.core.Result
import com.psy.dear.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(username: String, email: String, password: String): Result<Unit>
    suspend fun logout()
}

interface JournalRepository {
    fun getJournals(): Flow<List<Journal>>
    suspend fun syncJournals(): Result<Unit>
    suspend fun createJournal(title: String, content: String, mood: String): Result<Unit>
    suspend fun getGrowthStatistics(): Result<GrowthStatistics>
}

interface UserRepository {
    suspend fun getProfile(): Result<User>
}

interface ChatRepository {
    fun getChatHistory(): Flow<List<ChatMessage>>
    suspend fun sendMessage(message: String): Result<Unit>
}

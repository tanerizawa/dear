package com.psy.dear.di

import com.psy.dear.core.Result
import com.psy.dear.data.repository.*
import com.psy.dear.domain.model.*
import com.psy.dear.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

// Modul ini akan menggantikan RepositoryModule yang asli saat menjalankan instrumented test
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
abstract class TestAppModule {

    // Menyediakan implementasi FAKE dari repository untuk testing
    @Binds
    @Singleton
    abstract fun bindFakeAuthRepository(impl: FakeAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindFakeJournalRepository(impl: FakeJournalRepository): JournalRepository

    @Binds
    @Singleton
    abstract fun bindFakeChatRepository(impl: FakeChatRepository): ChatRepository

    @Binds
    @Singleton
    abstract fun bindFakeUserRepository(impl: FakeUserRepository): UserRepository
}

// Implementasi Fake untuk UI Test (perlu dibuat di sini karena visibility)
@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {
    private val loggedIn = MutableStateFlow(false)
    override val isLoggedIn: Flow<Boolean> = loggedIn.asStateFlow()

    override suspend fun login(email: String, password: String): Result<Unit> {
        loggedIn.value = true
        return Result.Success(Unit)
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String
    ): Result<Unit> {
        loggedIn.value = true
        return Result.Success(Unit)
    }

    override suspend fun logout() {
        loggedIn.value = false
    }
}

@Singleton
class FakeJournalRepository @Inject constructor() : JournalRepository {
    private val journals = mutableListOf<Journal>()
    private val flow = MutableStateFlow<List<Journal>>(emptyList())

    override fun getJournals(): Flow<List<Journal>> = flow.asStateFlow()

    override fun getJournalById(id: String): Flow<Journal?> =
        flow.asStateFlow().map { list -> list.find { it.id == id } }

    override suspend fun syncJournals(): Result<Unit> = Result.Success(Unit)

    override suspend fun createJournal(
        title: String,
        content: String,
        mood: String
    ): Result<Unit> {
        val journal = Journal(UUID.randomUUID().toString(), title, content, mood, OffsetDateTime.now())
        journals.add(journal)
        flow.value = journals
        return Result.Success(Unit)
    }

    override suspend fun updateJournal(
        id: String,
        title: String,
        content: String,
        mood: String
    ): Result<Unit> {
        return Result.Success(Unit)
    }

    override suspend fun deleteJournal(id: String): Result<Unit> {
        journals.removeAll { it.id == id }
        flow.value = journals
        return Result.Success(Unit)
    }

    override suspend fun getGrowthStatistics(): Result<GrowthStatistics> =
        Result.Success(GrowthStatistics(journals.size, "-", 0.0))
}

@Singleton
class FakeChatRepository @Inject constructor() : ChatRepository {
    private val messages = mutableListOf<ChatMessage>()
    private val flow = MutableStateFlow<List<ChatMessage>>(emptyList())

    override fun getChatHistory(): Flow<List<ChatMessage>> = flow.asStateFlow()

    override suspend fun sendMessage(message: String): Result<Unit> {
        val userMsg = ChatMessage(UUID.randomUUID().toString(), "user", message, null, OffsetDateTime.now())
        val reply = ChatMessage(UUID.randomUUID().toString(), "assistant", "reply:$message", null, OffsetDateTime.now())
        messages.add(userMsg)
        messages.add(reply)
        flow.value = messages
        return Result.Success(Unit)
    }

    override suspend fun deleteMessage(id: String): Result<Unit> {
        messages.removeAll { it.id == id }
        flow.value = messages
        return Result.Success(Unit)
    }

    override suspend fun flagMessage(id: String, flagged: Boolean): Result<Unit> = Result.Success(Unit)
}

@Singleton
class FakeUserRepository @Inject constructor() : UserRepository {
    override suspend fun getProfile(): Result<User> =
        Result.Success(User("1", "Test", "test@example.com"))
}

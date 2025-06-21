package com.psy.dear.di

import com.psy.dear.data.repository.*
import com.psy.dear.domain.repository.*
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
}

// Implementasi Fake untuk UI Test (perlu dibuat di sini karena visibility)
@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {
    private var _isLoggedIn = false
    override val isLoggedIn: Flow<Boolean> = flowOf(_isLoggedIn)
    override suspend fun login(email: String, password: String): Result<Unit> {
        _isLoggedIn = true
        return Result.Success(Unit)
    }
    // Implementasi lainnya...
}

@Singleton
class FakeJournalRepository @Inject constructor() : JournalRepository {
    // Implementasi fake yang relevan untuk UI test
}

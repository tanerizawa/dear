package com.psy.dear.domain.use_case.auth

import com.psy.dear.core.Result
import com.psy.dear.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckAuthStatusUseCase @Inject constructor(private val repo: AuthRepository) {
    operator fun invoke(): Flow<Boolean> = repo.isLoggedIn
}

class LoginUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> = repo.login(email, password)
}

class RegisterUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(username: String, email: String, password: String): Result<Unit> = repo.register(username, email, password)
}

class LogoutUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.logout()
}

package com.psy.dear.data.repository

import com.psy.dear.core.Result
import com.psy.dear.domain.model.User
import com.psy.dear.domain.repository.UserRepository

class FakeUserRepository : UserRepository {
    var user: User = User("1", "TestUser", "test@example.com")
    var shouldReturnError = false

    override suspend fun getProfile(): Result<User> {
        return if (shouldReturnError) {
            Result.Error(Exception("Profile error"))
        } else {
            Result.Success(user)
        }
    }
}

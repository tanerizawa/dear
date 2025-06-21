package com.psy.dear.domain.use_case.user

import com.psy.dear.core.Result
import com.psy.dear.domain.model.User
import com.psy.dear.domain.repository.UserRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Result<User> {
        return repository.getProfile()
    }
}

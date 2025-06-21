package com.psy.dear.domain.use_case.onboarding

import com.psy.dear.data.datastore.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckOnboardingStatusUseCase @Inject constructor(
    private val prefs: UserPreferencesRepository
) {
    operator fun invoke(): Flow<Boolean> = prefs.onboardingCompleted
}

class CompleteOnboardingUseCase @Inject constructor(
    private val prefs: UserPreferencesRepository
) {
    suspend operator fun invoke() {
        prefs.setOnboardingCompleted()
    }
}

package com.psy.dear.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.dear.domain.use_case.auth.CheckAuthStatusUseCase
import com.psy.dear.domain.use_case.onboarding.CheckOnboardingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

sealed class AuthState {
    object Loading : AuthState()
    data class Result(val isLoggedIn: Boolean, val onboardingCompleted: Boolean) : AuthState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    checkAuthStatusUseCase: CheckAuthStatusUseCase,
    checkOnboardingStatusUseCase: CheckOnboardingStatusUseCase
) : ViewModel() {

    val authState: StateFlow<AuthState> = combine(
        checkAuthStatusUseCase(),
        checkOnboardingStatusUseCase()
    ) { isLoggedIn, onboardingCompleted ->
        AuthState.Result(isLoggedIn, onboardingCompleted)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthState.Loading
    )
}

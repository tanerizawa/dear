package com.psy.dear.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.dear.core.Result
import com.psy.dear.domain.model.User
import com.psy.dear.domain.use_case.auth.LogoutUseCase
import com.psy.dear.domain.use_case.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ProfileEvent {
    object LogoutSuccess : ProfileEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ProfileEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val result = getUserProfileUseCase()) {
                is Result.Success -> _state.value = ProfileState(user = result.data)
                is Result.Error -> _state.value = ProfileState(error = result.exception?.message)
                is Result.Loading -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _eventFlow.emit(ProfileEvent.LogoutSuccess)
        }
    }
}

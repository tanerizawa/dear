package com.psy.dear.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.dear.core.Result
import com.psy.dear.domain.use_case.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RegisterEvent {
    object RegisterSuccess : RegisterEvent()
    data class ShowError(val message: String) : RegisterEvent()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<RegisterEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            when(val result = registerUseCase(username, email, password)) {
                is Result.Success -> _eventFlow.emit(RegisterEvent.RegisterSuccess)
                is Result.Error -> _eventFlow.emit(RegisterEvent.ShowError(result.exception?.message ?: "Registration failed"))
                else -> {}
            }
        }
    }
}

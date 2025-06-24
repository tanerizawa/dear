package com.psy.dear.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.dear.core.Result
import com.psy.dear.core.ErrorMapper
import com.psy.dear.core.UiText
import com.psy.dear.domain.use_case.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginEvent {
    object LoginSuccess : LoginEvent()
    data class ShowError(val message: UiText) : LoginEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<LoginEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            when(val result = loginUseCase(email, password)) {
                is Result.Success -> _eventFlow.emit(LoginEvent.LoginSuccess)
                is Result.Error -> _eventFlow.emit(LoginEvent.ShowError(UiText.StringResource(ErrorMapper.map(result.exception))))
                else -> {}
            }
        }
    }
}

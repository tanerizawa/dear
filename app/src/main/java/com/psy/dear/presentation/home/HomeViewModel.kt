package com.psy.dear.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.dear.core.Result
import com.psy.dear.core.ErrorMapper
import com.psy.dear.core.UiText
import com.psy.dear.domain.model.Journal
import com.psy.dear.domain.use_case.journal.GetJournalsUseCase
import com.psy.dear.domain.use_case.journal.SyncJournalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val journals: List<Journal> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getJournalsUseCase: GetJournalsUseCase,
    private val syncJournalsUseCase: SyncJournalsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        getJournalsUseCase()
            .onEach { journals ->
                _state.update { it.copy(journals = journals, error = null) }
            }
            .launchIn(viewModelScope)

        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = syncJournalsUseCase()
            _state.update {
                it.copy(
                    isLoading = false,
                    error = if (result is Result.Error) UiText.StringResource(ErrorMapper.map(result.exception)) else null
                )
            }
        }
    }
}

package com.psy.dear.presentation.journal_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.dear.core.Result
import com.psy.dear.core.ErrorMapper
import com.psy.dear.core.UiText
import com.psy.dear.domain.model.Journal
import com.psy.dear.domain.use_case.journal.DeleteJournalUseCase
import com.psy.dear.domain.use_case.journal.GetJournalByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailEvent {
    object DeleteSuccess : DetailEvent()
    data class ShowError(val message: UiText) : DetailEvent()
}

@HiltViewModel
class JournalDetailViewModel @Inject constructor(
    private val getJournalByIdUseCase: GetJournalByIdUseCase,
    private val deleteJournalUseCase: DeleteJournalUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val journalId: String = checkNotNull(savedStateHandle["journalId"])

    val journal: StateFlow<Journal?> = getJournalByIdUseCase(journalId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    private val _eventFlow = MutableSharedFlow<DetailEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun deleteJournal() {
        viewModelScope.launch {
            when (val result = deleteJournalUseCase(journalId)) {
                is Result.Success -> _eventFlow.emit(DetailEvent.DeleteSuccess)
                is Result.Error -> _eventFlow.emit(DetailEvent.ShowError(UiText.StringResource(ErrorMapper.map(result.exception))))
                else -> {}
            }
        }
    }
}

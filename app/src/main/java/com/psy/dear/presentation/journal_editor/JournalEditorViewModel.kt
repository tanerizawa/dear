package com.psy.dear.presentation.journal_editor

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.dear.core.Result
import com.psy.dear.domain.use_case.journal.GetJournalByIdUseCase
import com.psy.dear.domain.use_case.journal.SaveJournalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EditorEvent {
    object SaveSuccess : EditorEvent()
    data class ShowError(val message: String) : EditorEvent()
}

@HiltViewModel
class JournalEditorViewModel @Inject constructor(
    private val saveJournalUseCase: SaveJournalUseCase,
    private val getJournalByIdUseCase: GetJournalByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _title = mutableStateOf("")
    val title: State<String> = _title

    private val _content = mutableStateOf("")
    val content: State<String> = _content

    private val _mood = mutableStateOf("Netral")
    val mood: State<String> = _mood

    private val _eventFlow = MutableSharedFlow<EditorEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentJournalId: String? = null

    init {
        savedStateHandle.get<String>("journalId")?.let { journalId ->
            if (journalId != "null") {
                currentJournalId = journalId
                viewModelScope.launch {
                    getJournalByIdUseCase(journalId).onEach { journal ->
                        journal?.let {
                            _title.value = it.title
                            _content.value = it.content
                            _mood.value = it.mood
                        }
                    }.launchIn(this)
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onContentChange(newContent: String) {
        _content.value = newContent
    }

    fun saveJournal(title: String, content: String, mood: String) {
        viewModelScope.launch {
            val result = saveJournalUseCase(
                id = currentJournalId,
                title = title,
                content = content,
                mood = mood
            )
            when (result) {
                is Result.Success -> _eventFlow.emit(EditorEvent.SaveSuccess)
                is Result.Error -> _eventFlow.emit(EditorEvent.ShowError(result.exception?.message ?: "Gagal menyimpan"))
                else -> {}
            }
        }
    }
}

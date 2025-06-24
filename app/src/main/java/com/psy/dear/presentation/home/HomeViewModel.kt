package com.psy.dear.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.dear.core.Result
import com.psy.dear.core.ErrorMapper
import com.psy.dear.core.UiText
import com.psy.dear.domain.model.*
import com.psy.dear.domain.use_case.journal.GetJournalsUseCase
import com.psy.dear.domain.use_case.journal.SyncJournalsUseCase
import com.psy.dear.domain.use_case.content.GetArticlesUseCase
import com.psy.dear.domain.use_case.content.GetAudioTracksUseCase
import com.psy.dear.domain.use_case.content.GetQuotesUseCase
import com.psy.dear.domain.use_case.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val journals: List<Journal> = emptyList(),
    val articles: List<Article> = emptyList(),
    val audio: List<AudioTrack> = emptyList(),
    val quotes: List<MotivationalQuote> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val username: String = "User"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getJournalsUseCase: GetJournalsUseCase,
    private val syncJournalsUseCase: SyncJournalsUseCase,
    getArticlesUseCase: GetArticlesUseCase,
    getAudioTracksUseCase: GetAudioTracksUseCase,
    getQuotesUseCase: GetQuotesUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        getJournalsUseCase()
            .onEach { journals ->
                _state.update { it.copy(journals = journals, error = null) }
            }
            .launchIn(viewModelScope)

        getArticlesUseCase()
            .onEach { articles ->
                _state.update { it.copy(articles = articles) }
            }
            .launchIn(viewModelScope)

        getAudioTracksUseCase()
            .onEach { tracks ->
                _state.update { it.copy(audio = tracks) }
            }
            .launchIn(viewModelScope)

        getQuotesUseCase()
            .onEach { quotes ->
                _state.update { it.copy(quotes = quotes) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            when (val result = getUserProfileUseCase()) {
                is Result.Success -> _state.update { it.copy(username = result.data.username) }
                else -> {}
            }
        }

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

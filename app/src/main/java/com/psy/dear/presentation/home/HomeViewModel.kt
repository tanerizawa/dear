package com.psy.dear.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.dear.core.Result
import com.psy.dear.core.UiText
import com.psy.dear.domain.model.*
import com.psy.dear.domain.use_case.journal.GetJournalsUseCase
import com.psy.dear.domain.use_case.journal.SyncJournalsUseCase
import com.psy.dear.domain.use_case.content.GetArticlesUseCase
import com.psy.dear.domain.use_case.content.GetAudioTracksUseCase
import com.psy.dear.domain.use_case.content.GetMoodMusicUseCase
import com.psy.dear.domain.use_case.content.GetQuotesUseCase
import com.psy.dear.domain.use_case.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject
import com.psy.dear.R // Pastikan import R sudah ada

data class HomeState(
    val journals: List<Journal> = emptyList(),
    val articles: List<Article> = emptyList(),
    val audio: List<AudioTrack> = emptyList(),
    val moodTracks: List<AudioTrack> = emptyList(),
    val quotes: List<MotivationalQuote> = emptyList(),
    val isRefreshing: Boolean = false, // Indicates whether a refresh is in progress
    val username: String = "User"
)

// Definisikan event untuk komunikasi sekali jalan ke UI
sealed class UiEvent {
    data class ShowSnackbar(val message: UiText) : UiEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    getJournalsUseCase: GetJournalsUseCase,
    private val syncJournalsUseCase: SyncJournalsUseCase,
    getArticlesUseCase: GetArticlesUseCase,
    getAudioTracksUseCase: GetAudioTracksUseCase,
    private val getMoodMusicUseCase: GetMoodMusicUseCase,
    getQuotesUseCase: GetQuotesUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    // Channel untuk mengirim event seperti Snackbar
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var moodJob: Job? = null

    init {
        // Alur ini hanya mengambil dari database lokal, tidak akan pernah gagal
        getJournalsUseCase()
            .onEach { journals ->
                _state.update { it.copy(journals = journals) }
                val mood = journals.firstOrNull()?.mood
                moodJob?.cancel()
                if (mood != null) {
                    moodJob = getMoodMusicUseCase(mood)
                        .onEach { tracks ->
                            _state.update { it.copy(moodTracks = tracks) }
                        }
                        .launchIn(viewModelScope)
                } else {
                    _state.update { it.copy(moodTracks = emptyList()) }
                }
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

        // Ambil profil pengguna sekali saja
        viewModelScope.launch {
            when (val result = getUserProfileUseCase()) {
                is Result.Success -> _state.update {
                    it.copy(username = result.data?.username ?: "User")
                }
                else -> { /* Biarkan, jangan tampilkan error block */ }
            }
        }

        // Lakukan sinkronisasi pertama kali saat ViewModel dibuat
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            // Tampilkan indikator refresh (untuk pull-to-refresh)
            _state.update { it.copy(isRefreshing = true) }

            // Panggil use case sinkronisasi
            val result = syncJournalsUseCase()

            // Sembunyikan indikator refresh
            _state.update { it.copy(isRefreshing = false) }

            // Jika GAGAL, kirim event untuk tampilkan Snackbar, BUKAN error besar
            if (result is Result.Error) {
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(
                        // Kita bisa beri pesan lebih spesifik
                        message = UiText.StringResource(R.string.error_network)
                    )
                )
            }

            val mood = state.value.journals.firstOrNull()?.mood
            moodJob?.cancel()
            if (mood != null) {
                moodJob = getMoodMusicUseCase(mood)
                    .onEach { tracks ->
                        _state.update { it.copy(moodTracks = tracks) }
                    }
                    .launchIn(viewModelScope)
            } else {
                _state.update { it.copy(moodTracks = emptyList()) }
            }
        }
    }
}
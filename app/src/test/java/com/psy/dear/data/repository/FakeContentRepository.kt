package com.psy.dear.data.repository

import com.psy.dear.domain.model.Article
import com.psy.dear.domain.model.AudioTrack
import com.psy.dear.domain.model.MotivationalQuote
import com.psy.dear.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeContentRepository : ContentRepository {
    private val articlesFlow = MutableStateFlow<List<Article>>(emptyList())
    private val audioFlow = MutableStateFlow<List<AudioTrack>>(emptyList())
    private val moodMusicFlow = MutableStateFlow<List<AudioTrack>>(emptyList())
    private val quotesFlow = MutableStateFlow<List<MotivationalQuote>>(emptyList())

    fun setArticles(list: List<Article>) { articlesFlow.value = list }
    fun setAudio(list: List<AudioTrack>) { audioFlow.value = list }
    fun setMoodMusic(list: List<AudioTrack>) { moodMusicFlow.value = list }
    fun setQuotes(list: List<MotivationalQuote>) { quotesFlow.value = list }

    override fun getArticles(): Flow<List<Article>> = articlesFlow.asStateFlow()
    override fun getAudioTracks(): Flow<List<AudioTrack>> = audioFlow.asStateFlow()
    override fun getMoodMusic(mood: String): Flow<List<AudioTrack>> = moodMusicFlow.asStateFlow()
    override fun getQuotes(): Flow<List<MotivationalQuote>> = quotesFlow.asStateFlow()
}

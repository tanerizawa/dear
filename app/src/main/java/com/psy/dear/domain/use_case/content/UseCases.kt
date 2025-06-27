package com.psy.dear.domain.use_case.content

import com.psy.dear.domain.model.Article
import com.psy.dear.domain.model.AudioTrack
import com.psy.dear.domain.model.MotivationalQuote
import com.psy.dear.domain.model.Journal
import com.psy.dear.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArticlesUseCase @Inject constructor(private val repo: ContentRepository) {
    operator fun invoke(): Flow<List<Article>> = repo.getArticles()
}

class GetAudioTracksUseCase @Inject constructor(private val repo: ContentRepository) {
    operator fun invoke(): Flow<List<AudioTrack>> = repo.getAudioTracks()
}

class GetMoodMusicUseCase @Inject constructor(private val repo: ContentRepository) {
    operator fun invoke(mood: String): Flow<List<AudioTrack>> = repo.getMoodMusic(mood)
}

class GetRecommendedMusicUseCase @Inject constructor(private val repo: ContentRepository) {
    operator fun invoke(journals: List<Journal>): Flow<List<AudioTrack>> {
        // Saat ini daftar jurnal belum digunakan di repository, namun disediakan
        // agar logika rekomendasi dapat memanfaatkannya di masa depan
        return repo.getRecommendedMusic()
    }
}

class GetQuotesUseCase @Inject constructor(private val repo: ContentRepository) {
    operator fun invoke(): Flow<List<MotivationalQuote>> = repo.getQuotes()
}

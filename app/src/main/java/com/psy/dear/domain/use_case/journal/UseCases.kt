package com.psy.dear.domain.use_case.journal

import com.psy.dear.core.Result
import com.psy.dear.domain.model.GrowthStatistics
import com.psy.dear.domain.model.Journal
import com.psy.dear.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetJournalsUseCase @Inject constructor(private val repo: JournalRepository) {
    operator fun invoke(): Flow<List<Journal>> = repo.getJournals()
}

class SyncJournalsUseCase @Inject constructor(private val repo: JournalRepository) {
    suspend operator fun invoke(): Result<Unit> = repo.syncJournals()
}

class SaveJournalUseCase @Inject constructor(private val repo: JournalRepository) {
    suspend operator fun invoke(
        id: String? = null,
        title: String,
        content: String,
        mood: String
    ): Result<Unit> {
        if (title.isBlank() || content.isBlank()) {
            return Result.Error(IllegalArgumentException("Judul dan konten tidak boleh kosong."))
        }
        return if (id == null) {
            repo.createJournal(title, content, mood)
        } else {
            repo.updateJournal(id, title, content, mood)
        }
    }
}

class GetJournalByIdUseCase @Inject constructor(private val repo: JournalRepository) {
    operator fun invoke(id: String): Flow<Journal?> = repo.getJournalById(id)
}

class DeleteJournalUseCase @Inject constructor(private val repo: JournalRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repo.deleteJournal(id)
}

class GetGrowthStatisticsUseCase @Inject constructor(private val repo: JournalRepository) {
    suspend operator fun invoke(): Result<GrowthStatistics> = repo.getGrowthStatistics()
}

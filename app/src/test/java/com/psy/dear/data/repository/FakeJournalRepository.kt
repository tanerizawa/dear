package com.psy.dear.data.repository

import com.psy.dear.core.Result
import com.psy.dear.domain.model.GrowthStatistics
import com.psy.dear.domain.model.Journal
import com.psy.dear.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.OffsetDateTime

class FakeJournalRepository : JournalRepository {

    private val journals = mutableListOf<Journal>()
    private val journalsFlow = MutableStateFlow<List<Journal>>(emptyList())
    private var shouldReturnError = false

    fun setShouldReturnError(value: Boolean) {
        shouldReturnError = value
    }

    fun addJournal(journal: Journal) {
        journals.add(journal)
        refreshFlow()
    }

    private fun refreshFlow() {
        journalsFlow.value = journals
    }

    override fun getJournals(): Flow<List<Journal>> = journalsFlow.asStateFlow()

    override suspend fun syncJournals(): Result<Unit> {
        return if (shouldReturnError) Result.Error(Exception("Sync failed")) else Result.Success(Unit)
    }

    override suspend fun createJournal(title: String, content: String, mood: String): Result<Unit> {
        if(shouldReturnError) return Result.Error(Exception("Create failed"))
        journals.add(Journal(id = (journals.size + 1).toString(), title, content, mood, OffsetDateTime.now()))
        refreshFlow()
        return Result.Success(Unit)
    }

    override suspend fun getGrowthStatistics(): Result<GrowthStatistics> {
        return Result.Success(GrowthStatistics(journals.size, "Senang", 0.8))
    }
}

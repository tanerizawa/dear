package com.psy.dear.presentation.home

import app.cash.turbine.test
import com.psy.dear.data.repository.FakeJournalRepository
import com.psy.dear.domain.model.Journal
import com.psy.dear.domain.use_case.journal.GetJournalsUseCase
import com.psy.dear.domain.use_case.journal.SyncJournalsUseCase
import com.psy.dear.util.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.OffsetDateTime

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var viewModel: HomeViewModel
    private lateinit var fakeRepository: FakeJournalRepository
    private lateinit var getJournalsUseCase: GetJournalsUseCase
    private lateinit var syncJournalsUseCase: SyncJournalsUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeJournalRepository()
        getJournalsUseCase = GetJournalsUseCase(fakeRepository)
        syncJournalsUseCase = SyncJournalsUseCase(fakeRepository)

        // Tambahkan beberapa data dummy
        fakeRepository.addJournal(Journal("1", "Test 1", "Content 1", "Happy", OffsetDateTime.now()))

        viewModel = HomeViewModel(getJournalsUseCase, syncJournalsUseCase)
    }

    @Test
    fun `state reflects journals from repository on init`() = runTest {
        // ViewModel diinisialisasi di setUp, jadi kita langsung tes state-nya
        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(1, initialState.journals.size)
            assertEquals("Test 1", initialState.journals[0].title)
            assertFalse(initialState.isLoading)
            assertNull(initialState.error)
        }
    }

    @Test
    fun `refresh updates loading state and handles success`() = runTest {
        viewModel.state.test {
            // Abaikan state awal
            awaitItem()

            // Panggil refresh
            viewModel.refresh()

            // Harapannya state menjadi loading
            var loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            // Setelah operasi selesai, state loading kembali false
            loadingState = awaitItem()
            assertFalse(loadingState.isLoading)
            assertNull(loadingState.error)
        }
    }

    @Test
    fun `refresh handles error from repository`() = runTest {
        // Atur repository agar mengembalikan error
        fakeRepository.setShouldReturnError(true)

        viewModel.state.test {
            awaitItem() // Abaikan state awal

            viewModel.refresh()

            awaitItem() // State loading

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertEquals("Sync failed", errorState.error)
        }
    }
}

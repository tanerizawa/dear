package com.psy.dear.presentation.home

import app.cash.turbine.test
import com.psy.dear.data.repository.FakeJournalRepository
import com.psy.dear.data.repository.FakeContentRepository
import com.psy.dear.data.repository.FakeUserRepository
import com.psy.dear.domain.model.Journal
import com.psy.dear.domain.model.Article
import com.psy.dear.core.UiText
import com.psy.dear.R
import com.psy.dear.domain.use_case.journal.GetJournalsUseCase
import com.psy.dear.domain.use_case.journal.SyncJournalsUseCase
import com.psy.dear.domain.use_case.content.GetArticlesUseCase
import com.psy.dear.domain.use_case.content.GetAudioTracksUseCase
import com.psy.dear.domain.use_case.content.GetQuotesUseCase
import com.psy.dear.domain.use_case.user.GetUserProfileUseCase
import com.psy.dear.util.TestCoroutineRule
import com.psy.dear.presentation.home.UiEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    private lateinit var fakeContentRepo: FakeContentRepository
    private lateinit var fakeUserRepo: FakeUserRepository
    private lateinit var getJournalsUseCase: GetJournalsUseCase
    private lateinit var syncJournalsUseCase: SyncJournalsUseCase
    private lateinit var getArticles: GetArticlesUseCase
    private lateinit var getAudio: GetAudioTracksUseCase
    private lateinit var getQuotes: GetQuotesUseCase
    private lateinit var getUserProfile: GetUserProfileUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeJournalRepository()
        fakeContentRepo = FakeContentRepository()
        fakeUserRepo = FakeUserRepository()
        getJournalsUseCase = GetJournalsUseCase(fakeRepository)
        syncJournalsUseCase = SyncJournalsUseCase(fakeRepository)
        getArticles = GetArticlesUseCase(fakeContentRepo)
        getAudio = GetAudioTracksUseCase(fakeContentRepo)
        getQuotes = GetQuotesUseCase(fakeContentRepo)
        getUserProfile = GetUserProfileUseCase(fakeUserRepo)

        // Tambahkan beberapa data dummy
        fakeRepository.addJournal(Journal("1", "Test 1", "Content 1", "Happy", OffsetDateTime.now()))

        viewModel = HomeViewModel(
            getJournalsUseCase,
            syncJournalsUseCase,
            getArticles,
            getAudio,
            getQuotes,
            getUserProfile
        )
    }

    @Test
    fun `state reflects journals from repository on init`() = runTest {
        // ViewModel diinisialisasi di setUp, jadi kita langsung tes state-nya
        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(1, initialState.journals.size)
            assertEquals("Test 1", initialState.journals[0].title)
            assertFalse(initialState.isRefreshing)
            assertEquals("TestUser", initialState.username)
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
            assertTrue(loadingState.isRefreshing)

            // Setelah operasi selesai, state loading kembali false
            loadingState = awaitItem()
            assertFalse(loadingState.isRefreshing)
        }
    }

    @Test
    fun `refresh handles error from repository`() = runTest {
        // Atur repository agar mengembalikan error
        fakeRepository.setShouldReturnError(true)

        viewModel.eventFlow.test {
            val stateJob = launch {
                viewModel.state.test {
                    awaitItem() // Abaikan state awal

                    viewModel.refresh()

                    awaitItem() // State loading

                    val errorState = awaitItem()
                    assertFalse(errorState.isRefreshing)
                }
            }

            val event = awaitItem()
            assertEquals(
                UiEvent.ShowSnackbar(UiText.StringResource(R.string.error_network)),
                event
            )
            stateJob.cancel()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `articles emitted to state`() = runTest {
        fakeContentRepo.setArticles(listOf(Article("1", "A", "url")))

        viewModel.state.test {
            val initial = awaitItem()
            assertEquals(1, initial.articles.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

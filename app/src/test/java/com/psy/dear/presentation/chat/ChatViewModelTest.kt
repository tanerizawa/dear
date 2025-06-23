package com.psy.dear.presentation.chat

import app.cash.turbine.test
import com.psy.dear.core.Result
import com.psy.dear.core.UnauthorizedException
import com.psy.dear.data.repository.FakeChatRepository
import com.psy.dear.domain.use_case.chat.GetChatHistoryUseCase
import com.psy.dear.domain.use_case.chat.SendMessageUseCase
import com.psy.dear.domain.use_case.chat.DeleteMessageUseCase
import com.psy.dear.util.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class ChatViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var viewModel: ChatViewModel
    private lateinit var fakeRepository: FakeChatRepository
    private lateinit var getChatHistoryUseCase: GetChatHistoryUseCase
    private lateinit var sendMessageUseCase: SendMessageUseCase
    private lateinit var deleteMessageUseCase: DeleteMessageUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeChatRepository()
        getChatHistoryUseCase = GetChatHistoryUseCase(fakeRepository)
        sendMessageUseCase = SendMessageUseCase(fakeRepository)
        deleteMessageUseCase = DeleteMessageUseCase(fakeRepository)
        viewModel = ChatViewModel(
            getChatHistoryUseCase,
            sendMessageUseCase,
            deleteMessageUseCase
        )
    }

    @Test
    fun `navigate to login on unauthorized error`() = runTest {
        fakeRepository.sendResult = Result.Error(UnauthorizedException())

        viewModel.onEvent(ChatEvent.OnMessageChange("hi"))
        viewModel.eventFlow.test {
            viewModel.onEvent(ChatEvent.SendMessage)
            assertEquals(ChatUiEvent.NavigateToLogin, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `enter selection activates mode and stores id`() = runTest {
        viewModel.onEvent(ChatEvent.EnterSelection("1"))

        assertEquals(true, viewModel.uiState.selectionMode)
        assertEquals(setOf("1"), viewModel.uiState.selectedIds)
    }

    @Test
    fun `toggle selection adds and removes ids`() = runTest {
        viewModel.onEvent(ChatEvent.EnterSelection("1"))

        viewModel.onEvent(ChatEvent.ToggleSelection("2"))
        assertEquals(setOf("1", "2"), viewModel.uiState.selectedIds)
        assertEquals(true, viewModel.uiState.selectionMode)

        viewModel.onEvent(ChatEvent.ToggleSelection("1"))
        assertEquals(setOf("2"), viewModel.uiState.selectedIds)
        assertEquals(true, viewModel.uiState.selectionMode)

        viewModel.onEvent(ChatEvent.ToggleSelection("2"))
        assertEquals(emptySet(), viewModel.uiState.selectedIds)
        assertEquals(false, viewModel.uiState.selectionMode)
    }

    @Test
    fun `delete selected removes messages and clears selection`() = runTest {
        viewModel.onEvent(ChatEvent.EnterSelection("1"))
        viewModel.onEvent(ChatEvent.ToggleSelection("2"))

        viewModel.onEvent(ChatEvent.DeleteSelected)

        assertEquals(listOf("1", "2"), fakeRepository.deletedIds)
        assertEquals(false, viewModel.uiState.selectionMode)
        assertEquals(emptySet(), viewModel.uiState.selectedIds)
    }
}

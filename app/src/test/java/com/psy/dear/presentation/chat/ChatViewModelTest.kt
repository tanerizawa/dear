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
}

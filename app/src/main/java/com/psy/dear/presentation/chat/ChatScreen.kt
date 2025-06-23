@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

package com.psy.dear.presentation.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel
import com.psy.dear.presentation.chat.ChatBubbleTail
import androidx.navigation.NavController
import com.psy.dear.domain.model.ChatMessage
import com.psy.dear.ui.theme.ChatAppBar
import com.psy.dear.ui.theme.ChatBackground
import com.psy.dear.ui.theme.OtherBubble
import com.psy.dear.ui.theme.UserBubble
import com.psy.dear.ui.theme.IconInactive
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ChatScreen(
    navController: NavController,
    onNavigateToLogin: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                ChatUiEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ChatTopBar(
                selectionMode = uiState.selectionMode,
                selectedCount = uiState.selectedIds.size,
                onBack = { viewModel.onEvent(ChatEvent.ExitSelection) },
                onDelete = { viewModel.onEvent(ChatEvent.DeleteSelected) }
            )
        },
        bottomBar = {
            ChatInputBar(
                text = uiState.currentMessage,
                onTextChange = { viewModel.onEvent(ChatEvent.OnMessageChange(it)) },
                onSendClick = { viewModel.onEvent(ChatEvent.SendMessage) },
                isSending = uiState.isSending
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatBackground)
                .padding(paddingValues)
        ) {
            if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                items(uiState.messages) { message ->
                    ChatMessageItem(
                        message = message,
                        selected = uiState.selectedIds.contains(message.id),
                        selectionMode = uiState.selectionMode,
                        onClick = {
                            if (uiState.selectionMode) {
                                viewModel.onEvent(ChatEvent.ToggleSelection(message.id))
                            }
                        },
                        onLongPress = { viewModel.onEvent(ChatEvent.EnterSelection(message.id)) },
                        onDelete = { viewModel.onEvent(ChatEvent.DeleteMessage(message.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatTopBar(
    selectionMode: Boolean,
    selectedCount: Int,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ChatAppBar,
            navigationIconContentColor = Color.White,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        title = {
            Text(if (selectionMode) "$selectedCount selected" else "Chat")
        },
        navigationIcon = {
            if (selectionMode) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (selectionMode) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    )
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ketik pesanmu...") },
            enabled = !isSending,
            maxLines = 5
        )
        Spacer(modifier = Modifier.width(8.dp))

        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            if (isSending) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            } else {
                IconButton(
                    onClick = onSendClick,
                    enabled = text.isNotBlank(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (text.isNotBlank()) ChatAppBar else Color.Transparent
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (text.isNotBlank()) Color.White else IconInactive
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDelete: () -> Unit
) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) ChatBubbleTail(color = OtherBubble, isUser = false)
        Surface(
            color = if (isUser) UserBubble else OtherBubble,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
            ) {
                Text(
                    text = message.content,
                    color = Color.Black,
                    modifier = Modifier.weight(1f, false)
                )
                Text(
                    text = message.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (isUser) ChatBubbleTail(color = UserBubble, isUser = true)
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "Dear is typing...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

package com.psy.dear.presentation.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.psy.dear.core.asString
import com.psy.dear.domain.model.ChatMessage
import kotlinx.coroutines.flow.collectLatest
import java.time.format.DateTimeFormatter

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
                .background(MaterialTheme.colorScheme.background) // Menggunakan warna latar dari tema
                .padding(paddingValues)
        ) {
            if (uiState.error != null) {
                Text(
                    text = uiState.error.asString(),
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
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
            containerColor = MaterialTheme.colorScheme.surface, // Sesuai tema
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            Text(if (selectionMode) "$selectedCount selected" else "Chat")
        },
        navigationIcon = {
            if (selectionMode) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (selectionMode) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete")
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
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message") },
            enabled = !isSending,
            maxLines = 5,
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            )
        )
        Spacer(modifier = Modifier.width(8.dp))

        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            if (isSending) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            } else {
                val containerColor = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Transparent
                val iconColor = if (text.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                IconButton(
                    onClick = onSendClick,
                    enabled = text.isNotBlank(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = containerColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = iconColor
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
        val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

        if (!isUser) ChatBubbleTail(color = bubbleColor, isUser = false)

        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 1.dp,
            border = if (!isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
            ) {
                Text(
                    text = message.content,
                    color = textColor
                )
                Text(
                    text = message.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (isUser) ChatBubbleTail(color = bubbleColor, isUser = true)
    }
}
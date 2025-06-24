package com.psy.dear.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.pullrefresh.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.psy.dear.domain.model.Journal
import com.psy.dear.core.asString
import com.psy.dear.presentation.components.FullScreenLoading
import com.psy.dear.presentation.components.InfoMessage
import com.psy.dear.presentation.components.PressableCard
import com.psy.dear.presentation.home.ArticleCard
import com.psy.dear.presentation.home.AudioCard
import com.psy.dear.presentation.home.GreetingCard
import com.psy.dear.presentation.home.JournalPromptCard
import com.psy.dear.presentation.home.MotivationCard
import com.psy.dear.presentation.navigation.Screen
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pullRefreshState = rememberPullRefreshState(state.isLoading, { viewModel.refresh() })

    Scaffold(
        topBar = { TopAppBar(title = { Text("Beranda") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.JournalEditor.createRoute(null))
            }) {
                Icon(Icons.Default.Add, contentDescription = "Entri Baru")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).pullRefresh(pullRefreshState)) {
            val error = state.error
            if (state.isLoading && state.journals.isEmpty()) {
                FullScreenLoading()
            } else {
                error?.let { InfoMessage(message = it.asString(), icon = Icons.Default.CloudOff) } ?: LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item { GreetingCard(state.username) }
                    item { JournalPromptCard { navController.navigate(Screen.JournalEditor.createRoute(null)) } }
                    items(state.articles) { ArticleCard(it) }
                    items(state.audio) { AudioCard(it) }
                    items(state.quotes) { MotivationCard(it) }
                    items(state.journals, key = { it.id }) { journal ->
                        JournalItem(
                            journal = journal,
                            onClick = { navController.navigate(Screen.JournalDetail.createRoute(journal.id)) }
                        )
                    }
                }
            }
            PullRefreshIndicator(state.isLoading, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
fun JournalItem(journal: Journal, onClick: () -> Unit) {
    PressableCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = journal.title, style = MaterialTheme.typography.titleLarge)
            Text(text = journal.mood, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = journal.createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

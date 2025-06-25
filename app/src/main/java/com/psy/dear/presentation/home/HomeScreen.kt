package com.psy.dear.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.pullrefresh.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.psy.dear.core.asString
import com.psy.dear.domain.model.Article
import com.psy.dear.domain.model.AudioTrack
import com.psy.dear.domain.model.Journal
import com.psy.dear.presentation.components.FullScreenLoading
import com.psy.dear.presentation.navigation.Screen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pullRefreshState = rememberPullRefreshState(state.isRefreshing, { viewModel.refresh() })
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message.asString(context)
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.JournalEditor.createRoute(null))
            }) {
                Icon(Icons.Outlined.Add, contentDescription = "Entri Baru")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            if (state.isRefreshing && state.journals.isEmpty() && state.articles.isEmpty()) {
                FullScreenLoading()
            } else {
                HomeDashboard(state = state, navController = navController)
            }

            PullRefreshIndicator(state.isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
private fun HomeDashboard(state: HomeState, navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item { GreetingCard(userName = state.username) }
        item { JournalPromptCard { navController.navigate(Screen.JournalEditor.createRoute(null)) } }

        if (state.moodTracks.isNotEmpty()) {
            item {
                MoodMusicCard(
                    track = state.moodTracks.first(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    onClick = {
                        val track = state.moodTracks.first()
                        navController.navigate(
                            Screen.AudioPlayer.createRoute(url = track.url, title = track.title)
                        )
                    }
                )
            }
        }

        if (state.quotes.isNotEmpty()) {
            item {
                SectionHeader(title = "Kutipan Hari Ini", modifier = Modifier.padding(top = 16.dp))
                MotivationCard(
                    quote = state.quotes.first(),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        if (state.journals.isNotEmpty()) {
            item {
                HorizontalSection(
                    title = "Jurnal Terakhir Anda",
                    items = state.journals,
                    itemContent = { journal ->
                        JournalItem(
                            journal = journal,
                            modifier = Modifier.width(280.dp),
                            onClick = { navController.navigate(Screen.JournalDetail.createRoute(journal.id)) }
                        )
                    },
                    onViewAll = { navController.navigate(Screen.JournalList.route) }
                )
            }
        }

        if (state.articles.isNotEmpty()) {
            item {
                HorizontalSection(
                    title = "Artikel Pilihan",
                    items = state.articles,
                    itemContent = { article ->
                        ArticleCard(
                            article = article,
                            modifier = Modifier.width(240.dp),
                            onClick = {
                                navController.navigate(
                                    Screen.ArticleDetail.createRoute(url = article.url, title = article.title)
                                )
                            }
                        )
                    },
                    onViewAll = { navController.navigate(Screen.ArticleList.route) }
                )
            }
        }

        if (state.audio.isNotEmpty()) {
            item {
                HorizontalSection(
                    title = "Audio Relaksasi",
                    items = state.audio,
                    itemContent = { track ->
                        AudioCard(
                            track = track,
                            modifier = Modifier.width(200.dp),
                            onClick = {
                                navController.navigate(
                                    Screen.AudioPlayer.createRoute(url = track.url, title = track.title)
                                )
                            }
                        )
                    },
                    onViewAll = { navController.navigate(Screen.AudioList.route) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
    )
}

@Composable
private fun <T> HorizontalSection(
    title: String,
    items: List<T>,
    itemContent: @Composable (T) -> Unit,
    onViewAll: () -> Unit
) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onViewAll) {
                Text("Lihat Semua")
            }
        }
        Spacer(Modifier.height(4.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                itemContent(item)
            }
        }
    }
}
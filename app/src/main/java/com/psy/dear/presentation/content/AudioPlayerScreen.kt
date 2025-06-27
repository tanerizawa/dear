package com.psy.dear.presentation.content

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.psy.dear.presentation.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    navController: NavController,
    trackTitle: String,
    trackUrl: String,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by homeViewModel.state.collectAsState()
    val tracks = remember(state.audio, state.recommendedTracks) {
        state.audio + state.recommendedTracks
    }
    var currentIndex by remember(trackUrl) {
        mutableStateOf(tracks.indexOfFirst { it.url == trackUrl }.coerceAtLeast(0))
    }
    val currentTrack = tracks.getOrNull(currentIndex)

    var isPlaying by remember { mutableStateOf(true) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(currentIndex) {
        val url = currentTrack?.url ?: trackUrl
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepare()
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                if (tracks.isNotEmpty()) {
                    currentIndex = (currentIndex + 1) % tracks.size
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error loading track", e)
        }
        player = mediaPlayer
        onDispose { mediaPlayer.release() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memutar Audio") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                text = currentTrack?.title ?: trackTitle,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Rekomendasi Spotify",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (tracks.isNotEmpty()) {
                        currentIndex = if (currentIndex - 1 < 0) tracks.size - 1 else currentIndex - 1
                    }
                }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Sebelumnya", modifier = Modifier.size(48.dp))
                }

                IconButton(onClick = {
                    player?.let {
                        if (isPlaying) {
                            it.pause()
                        } else {
                            it.start()
                        }
                        isPlaying = !isPlaying
                    }
                }, modifier = Modifier.size(72.dp)) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                IconButton(onClick = {
                    if (tracks.isNotEmpty()) {
                        currentIndex = (currentIndex + 1) % tracks.size
                    }
                }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Berikutnya", modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

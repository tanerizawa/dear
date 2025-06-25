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
    val tracks = state.audio + state.moodTracks
    var currentIndex by remember(tracks, trackUrl) {
        mutableStateOf(tracks.indexOfFirst { it.url == trackUrl }.takeIf { it >= 0 } ?: 0)
    }

    var isPlaying by remember { mutableStateOf(false) }
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(currentIndex, tracks) {
        val track = tracks.getOrNull(currentIndex)
        if (track != null) {
            try {
                mediaPlayer.reset()

                // Set listeners sebelum prepareAsync()
                mediaPlayer.setOnPreparedListener { mp ->
                    mp.start()
                    isPlaying = true
                }

                mediaPlayer.setOnErrorListener { mp, what, extra ->
                    Log.e("MediaPlayer", "Error what: $what, extra: $extra")
                    mp.reset()
                    isPlaying = false
                    true // menandakan error sudah ditangani
                }

                mediaPlayer.setOnCompletionListener {
                    if (tracks.isNotEmpty()) {
                        currentIndex = (currentIndex + 1) % tracks.size
                        isPlaying = false
                    }
                }

                mediaPlayer.setDataSource(track.url)
                mediaPlayer.prepareAsync()

            } catch (e: Exception) {
                Log.e("MediaPlayer", "Error setting data source", e)
            }
        }

        onDispose { }
    }

    DisposableEffect(Unit) {
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
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder ikon musik
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = tracks.getOrNull(currentIndex)?.title ?: trackTitle,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Artis Tidak Dikenal",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            Slider(value = 0.5f, onValueChange = {}) // Placeholder SeekBar

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (tracks.isNotEmpty()) {
                        currentIndex = if (currentIndex - 1 < 0) tracks.size - 1 else currentIndex - 1
                        isPlaying = true
                    }
                }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Sebelumnya", modifier = Modifier.size(48.dp))
                }

                IconButton(onClick = {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                        isPlaying = false
                    } else {
                        mediaPlayer.start()
                        isPlaying = true
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
                        isPlaying = true
                    }
                }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Berikutnya", modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

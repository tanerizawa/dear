package com.psy.dear.presentation.content

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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.psy.dear.presentation.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    navController: NavController,
    trackTitle: String,
    trackUrl: String, // Ini sekarang berisi videoId
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val state by homeViewModel.state.collectAsState()
    val tracks = state.audio + state.recommendedTracks
    var currentIndex by remember(tracks, trackUrl) {
        mutableStateOf(tracks.indexOfFirst { it.url == trackUrl }.takeIf { it >= 0 } ?: 0)
    }
    val currentTrack = tracks.getOrNull(currentIndex)

    var isPlaying by remember { mutableStateOf(true) }
    var player: YouTubePlayer? by remember { mutableStateOf(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Memuat ulang video ketika currentIndex berubah
    LaunchedEffect(currentIndex, player) {
        currentTrack?.let {
            player?.loadVideo(it.url, 0f)
        }
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
            // Gunakan AndroidView untuk menampung YouTubePlayerView
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                factory = { context ->
                    YouTubePlayerView(context).apply {
                        // --- PERBAIKAN DI SINI ---
                        // Biarkan library menginisialisasi dirinya sendiri secara otomatis
                        // dengan mengaitkannya ke lifecycle.
                        lifecycleOwner.lifecycle.addObserver(this)

                        // Cukup tambahkan listener untuk bereaksi saat pemutar siap.
                        // Jangan panggil initialize() secara manual.
                        addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                player = youTubePlayer
                                currentTrack?.let {
                                    youTubePlayer.loadVideo(it.url, 0f)
                                }
                            }

                            override fun onStateChange(
                                youTubePlayer: YouTubePlayer,
                                state: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
                            ) {
                                isPlaying = state == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING
                                if (state == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.ENDED) {
                                    if (tracks.isNotEmpty()) {
                                        currentIndex = (currentIndex + 1) % tracks.size
                                    }
                                }
                            }

                            override fun onError(
                                youTubePlayer: YouTubePlayer,
                                error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError
                            ) {
                                Log.e("YouTubePlayer", "Error: $error")
                            }
                        })
                    }
                }
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = currentTrack?.title ?: trackTitle,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Rekomendasi YouTube",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Kontrol Pemutar
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
                    if (isPlaying) {
                        player?.pause()
                    } else {
                        player?.play()
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

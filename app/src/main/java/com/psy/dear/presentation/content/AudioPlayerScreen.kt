package com.psy.dear.presentation.content

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.psy.dear.presentation.home.HomeViewModel
import java.io.IOException

// Enum untuk merepresentasikan state pemutar audio dengan lebih jelas
private enum class PlayerState {
    IDLE,       // Pemutar tidak melakukan apa-apa
    PREPARING,  // Sedang memuat lagu dari jaringan
    PLAYING,    // Sedang memutar lagu
    PAUSED      // Dijeda
}

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

    // Menggabungkan daftar lagu audio dan rekomendasi menjadi satu
    val tracks = remember(state.audio, state.recommendedTracks) {
        state.audio + state.recommendedTracks
    }

    // Mengelola indeks lagu yang sedang diputar
    var currentIndex by remember {
        mutableIntStateOf(tracks.indexOfFirst { it.url == trackUrl }.coerceAtLeast(0))
    }

    // Mengambil data lagu saat ini, aman dari NullPointerException
    val currentTrack = tracks.getOrNull(currentIndex)

    // State untuk mengelola kondisi pemutar: Playing, Paused, Preparing, dll.
    var playerState by remember { mutableStateOf(PlayerState.IDLE) }

    // Membuat satu instance MediaPlayer yang akan digunakan kembali.
    // Ini jauh lebih efisien daripada membuat instance baru setiap kali.
    val mediaPlayer = remember {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }
    }

    // Effect ini akan berjalan setiap kali lagu (currentIndex) berubah.
    // Ini adalah inti dari logika pemutaran audio.
    LaunchedEffect(currentIndex) {
        currentTrack?.url?.let { url ->
            playerState = PlayerState.PREPARING
            try {
                // Reset pemutar untuk memutar lagu baru
                mediaPlayer.reset()
                mediaPlayer.setDataSource(url)

                // Gunakan prepareAsync()! Ini adalah kunci untuk menghindari crash dan ANR.
                // Ia akan memuat audio di background.
                mediaPlayer.prepareAsync()

            } catch (e: IOException) {
                // Menangkap error jika URL tidak valid sejak awal
                Log.e("AudioPlayer", "Error setting data source", e)
                Toast.makeText(context, "Pratinjau audio tidak tersedia.", Toast.LENGTH_SHORT).show()
                playerState = PlayerState.IDLE
            }
        }
    }

    // Effect ini mengelola lifecycle dari MediaPlayer
    DisposableEffect(Unit) {
        // Listener ini akan dipanggil ketika prepareAsync() selesai.
        mediaPlayer.setOnPreparedListener {
            it.start()
            playerState = PlayerState.PLAYING
        }

        // Listener ini akan dipanggil ketika lagu selesai diputar.
        mediaPlayer.setOnCompletionListener {
            if (tracks.isNotEmpty()) {
                // Otomatis putar lagu berikutnya
                currentIndex = (currentIndex + 1) % tracks.size
            } else {
                playerState = PlayerState.IDLE
            }
        }

        // Listener ini adalah PENJAGA ANDA. Akan dipanggil jika terjadi error
        // saat memuat atau memutar lagu.
        mediaPlayer.setOnErrorListener { _, _, _ ->
            Log.e("AudioPlayer", "MediaPlayer error. URL mungkin tidak valid atau jaringan bermasalah.")
            Toast.makeText(context, "Gagal memutar audio.", Toast.LENGTH_SHORT).show()
            playerState = PlayerState.IDLE
            true // Menandakan bahwa error sudah ditangani
        }

        // Ketika layar ditutup, kita harus melepaskan resource MediaPlayer.
        onDispose {
            mediaPlayer.release()
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

            Spacer(Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (tracks.isNotEmpty()) {
                            // Logika untuk lagu sebelumnya
                            currentIndex = if (currentIndex - 1 < 0) tracks.size - 1 else currentIndex - 1
                        }
                    },
                    // Nonaktifkan tombol jika tidak ada lagu
                    enabled = tracks.isNotEmpty()
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Sebelumnya", modifier = Modifier.size(48.dp))
                }

                IconButton(
                    onClick = {
                        if (playerState == PlayerState.PLAYING) {
                            mediaPlayer.pause()
                            playerState = PlayerState.PAUSED
                        } else if (playerState == PlayerState.PAUSED) {
                            mediaPlayer.start()
                            playerState = PlayerState.PLAYING
                        }
                    },
                    modifier = Modifier.size(72.dp),
                    // Nonaktifkan tombol jika lagu sedang dimuat atau tidak ada
                    enabled = playerState == PlayerState.PLAYING || playerState == PlayerState.PAUSED
                ) {
                    // Tampilkan ikon yang sesuai dengan state pemutar
                    Icon(
                        if (playerState == PlayerState.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playerState == PlayerState.PLAYING) "Pause" else "Play",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                IconButton(
                    onClick = {
                        if (tracks.isNotEmpty()) {
                            // Logika untuk lagu berikutnya
                            currentIndex = (currentIndex + 1) % tracks.size
                        }
                    },
                    enabled = tracks.isNotEmpty()
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Berikutnya", modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}
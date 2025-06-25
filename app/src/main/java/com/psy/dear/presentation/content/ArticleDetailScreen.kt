package com.psy.dear.presentation.content

import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    navController: NavController,
    title: String,
    url: String
) {
    // State untuk WebView, termasuk URL yang akan dimuat
    val webViewState = rememberWebViewState(url = url)

    // --- REVISI DI SINI ---
    // Kita dapatkan status loading langsung dari webViewState
    val loadingState = webViewState.loadingState
    val isLoading = loadingState is LoadingState.Loading

    // Kita tidak perlu lagi membuat custom WebViewClient
    // val webViewClient = remember { ... } // HAPUS BLOK INI

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            WebView(
                state = webViewState,
                modifier = Modifier.fillMaxSize(),
                // --- REVISI DI SINI ---
                // Parameter client tidak lagi diperlukan karena kita tidak membuat client custom
                // client = webViewClient // HAPUS BARIS INI
            )

            // Tampilkan progress indicator jika sedang loading
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
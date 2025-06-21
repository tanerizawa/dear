package com.psy.dear.presentation.services

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Layanan") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            ServiceItem(title = "Tes Stres, Cemas, Depresi (DASS-42)") {
                // navController.navigate(...)
            }
            ServiceItem(title = "Tes Kepribadian (MBTI)") {
                // navController.navigate(...)
            }
        }
    }
}

@Composable
fun ServiceItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable(onClick = onClick)
    ) {
        Text(text = title, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
    }
}

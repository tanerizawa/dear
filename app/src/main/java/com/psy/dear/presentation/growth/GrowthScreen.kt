package com.psy.dear.presentation.growth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.psy.dear.domain.model.GrowthStatistics
import com.psy.dear.core.asString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthScreen(
    navController: NavController,
    viewModel: GrowthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Pertumbuhan") }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.stats != null -> StatisticsContent(stats = state.stats!!)
                else -> state.error?.let {
                    Text(text = it.asString(), modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
private fun StatisticsContent(stats: GrowthStatistics) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(title = "Total Entri Jurnal", value = stats.totalJournals.toString())
        StatCard(title = "Mood Paling Sering", value = stats.mostFrequentMood)
        StatCard(title = "Rata-rata Sentimen", value = String.format("%.2f", stats.averageSentiment))
    }
}

@Composable
private fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = value, style = MaterialTheme.typography.headlineLarge)
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

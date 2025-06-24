package com.psy.dear.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.psy.dear.domain.model.*

@Composable
fun GreetingCard(userName: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Hello, $userName",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun JournalPromptCard(onClick: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Tulis jurnal harianmu", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun ArticleCard(article: Article) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(article.title, style = MaterialTheme.typography.titleMedium)
            Text(article.url, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun AudioCard(track: AudioTrack) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(track.title, style = MaterialTheme.typography.titleMedium)
            Text(track.url, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun MotivationCard(quote: MotivationalQuote) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("\"${quote.text}\"", style = MaterialTheme.typography.bodyMedium)
            Text("- ${quote.author}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

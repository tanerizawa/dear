package com.psy.dear.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.psy.dear.domain.model.*
import com.psy.dear.presentation.components.PressableCard

@Composable
fun GreetingCard(userName: String) {
    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x334A6CFA),
                ambientColor = Color(0x1F4A6CFA),
                shape = shape
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFE0E7FF), Color(0xFFF3E8FF))
                    ),
                    shape = shape
                )
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Hello, $userName",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun JournalPromptCard(onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x334A6CFA),
                ambientColor = Color(0x1F4A6CFA),
                shape = shape
            ),
        shape = shape
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                "Tulis jurnal harianmu",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ArticleCard(article: Article, onClick: () -> Unit = {}) {
    PressableCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(article.title, style = MaterialTheme.typography.titleMedium)
            Text(article.url, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun AudioCard(track: AudioTrack, onClick: () -> Unit = {}) {
    PressableCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(track.title, style = MaterialTheme.typography.titleMedium)
            Text(track.url, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun MotivationCard(quote: MotivationalQuote, onClick: () -> Unit = {}) {
    PressableCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("\"${quote.text}\"", style = MaterialTheme.typography.bodyMedium)
            Text("- ${quote.author}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

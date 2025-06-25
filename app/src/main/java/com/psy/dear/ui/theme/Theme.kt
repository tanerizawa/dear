package com.psy.dear.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Skema untuk Light Mode
private val LightColorScheme = lightColorScheme(
    primary = LinkedinBlue,
    onPrimary = White,
    background = LinkedinBackground,
    onBackground = LinkedinBlack,
    surface = White, // Untuk Cards, TopAppBar, etc.
    onSurface = LinkedinBlack,
    onSurfaceVariant = LinkedinGrey, // Untuk teks/ikon sekunder
    outline = LinkedinLightGrey // Untuk border
)

// Skema untuk Dark Mode (bisa disesuaikan lebih lanjut)
private val DarkColorScheme = darkColorScheme(
    primary = LinkedinBlue,
    onPrimary = White,
    background = Color(0xFF1B1B1B),
    onBackground = Color(0xFFEAEAEA),
    surface = Color(0xFF2C2C2C),
    onSurface = Color(0xFFEAEAEA),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF444444)
)

@Composable
fun DearTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // ... (SideEffect untuk status bar bisa disesuaikan warnanya dengan colorScheme.surface)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Tipografi baru kita
        shapes = Shapes,         // Bentuk baru kita
        content = content
    )
}
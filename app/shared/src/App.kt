package com.example.testmmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun App() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF0E2E16),
            secondary = Color(0xFFB6F500),
            background = Color(0xFFEDEDEE),
            surface = Color(0xFFF7F7F8),
            onSurface = Color(0xFF060607),
        ),
    ) {
        TrainerHomeScreen()
    }
}

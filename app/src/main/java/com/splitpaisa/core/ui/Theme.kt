package com.splitpaisa.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006B5F),
    onPrimary = Color.White,
    secondary = Color(0xFF4CAF50),
    tertiary = Color(0xFF03A9F4),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4DD0E1),
    onPrimary = Color.Black,
    secondary = Color(0xFF81C784),
    tertiary = Color(0xFF4FC3F7),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

@Composable
fun PaisaSplitTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        content = content
    )
}

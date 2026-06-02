package org.example.dementia_tester_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.example.dementia_tester_app.ui.components.FormColors

private val LightColorScheme = lightColorScheme(
    primary = FormColors.green,
    onPrimary = Color.White,
    primaryContainer = FormColors.green,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF03DAC5),
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = FormColors.errorColor
)

private val DarkColorScheme = darkColorScheme(
    primary = FormColors.green,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E1E1E),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF03DAC5),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color.White,
    onSurface = Color.White,
    error = FormColors.errorColor
)

@Composable
fun DementiaTesterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}


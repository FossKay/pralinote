package com.example.pralinote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicColorScheme

@Composable
fun PralinoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // dynamicColor: Boolean = true,
    color: Color?,
    content: @Composable () -> Unit
) {
    val colorScheme = rememberDynamicColorScheme(
        primary = color ?: Primary,
        isDark = darkTheme,
        isAmoled = false,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
package com.example.pralinote.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
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
package com.example.flixr.ui.theme

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

private val DarkColorScheme =
    darkColorScheme(
        primary = FlixrPrimaryDark,
        onPrimary = Color(0xFF400025),
        primaryContainer = Color(0xFF8B2255),
        onPrimaryContainer = Color(0xFFFFD9E8),
        secondary = PurpleGrey80,
        tertiary = Pink80,
        background = Color(0xFF13101A),
        surface = Color(0xFF1B1624),
        surfaceVariant = Color(0xFF2D2839),
        onSurfaceVariant = Color(0xFFCAC4D4),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = FlixrPrimaryLight,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFD8EB),
        onPrimaryContainer = Color(0xFF3E001B),
        secondary = PurpleGrey40,
        tertiary = Pink40,
        background = FlixrBackgroundLight,
        surface = FlixrSurfaceLight,
        surfaceVariant = Color(0xFFE8DEF8),
        onSurfaceVariant = Color(0xFF49454E),
    )

@Composable
fun FlixrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
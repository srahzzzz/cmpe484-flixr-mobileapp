package com.example.flixr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Full-screen wash behind tabs and stacked flows (light: lavender→blush; dark: deep plum). */
@Composable
fun flixrMainSurfaceGradientBrush(): Brush {
    val dark = isSystemInDarkTheme()
    return remember(dark) {
        if (dark) {
            Brush.verticalGradient(
                colors =
                    listOf(
                        FlixrScreenGradientTopDark,
                        FlixrScreenGradientMidDark,
                        FlixrScreenGradientBottomDark,
                        FlixrGradientBottomLeft.copy(alpha = 0.2f),
                    ),
            )
        } else {
            Brush.verticalGradient(
                colors =
                    listOf(
                        FlixrScreenGradientTopLight,
                        FlixrScreenGradientMidLight,
                        FlixrScreenGradientBottomLight,
                        FlixrAccent.copy(alpha = 0.1f),
                    ),
            )
        }
    }
}

@Composable
fun flixrHeroPrimaryText(): Color =
    if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onSurface

@Composable
fun flixrHeroSecondaryText(): Color =
    if (isSystemInDarkTheme()) FlixrMuted else MaterialTheme.colorScheme.onSurfaceVariant

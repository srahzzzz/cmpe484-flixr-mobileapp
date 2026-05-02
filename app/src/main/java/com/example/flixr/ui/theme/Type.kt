package com.example.flixr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.flixr.R

// Offline bundled Poppins (fast startup, no network).
private val flixrFont = FontFamily(
    Font(R.font.poppins_regular, weight = FontWeight.Normal),
    Font(R.font.poppins_semibold, weight = FontWeight.SemiBold),
    Font(R.font.poppins_bold, weight = FontWeight.Bold),
)

private val baseline = Typography()

private fun TextStyle.withFlixrFont(): TextStyle = copy(fontFamily = flixrFont)

/** App-wide typography: Material roles + Poppins for a cohesive streaming-app feel. */
val Typography =
    Typography(
        displayLarge = baseline.displayLarge.withFlixrFont(),
        displayMedium = baseline.displayMedium.withFlixrFont(),
        displaySmall = baseline.displaySmall.withFlixrFont(),
        headlineLarge = baseline.headlineLarge.withFlixrFont(),
        headlineMedium = baseline.headlineMedium.withFlixrFont(),
        headlineSmall = baseline.headlineSmall.withFlixrFont(),
        titleLarge = baseline.titleLarge.withFlixrFont().copy(fontWeight = FontWeight.Bold),
        titleMedium = baseline.titleMedium.withFlixrFont().copy(fontWeight = FontWeight.SemiBold),
        titleSmall = baseline.titleSmall.withFlixrFont().copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = baseline.bodyLarge.withFlixrFont(),
        bodyMedium = baseline.bodyMedium.withFlixrFont(),
        bodySmall = baseline.bodySmall.withFlixrFont(),
        labelLarge = baseline.labelLarge.withFlixrFont(),
        labelMedium = baseline.labelMedium.withFlixrFont(),
        labelSmall = baseline.labelSmall.withFlixrFont(),
    )

package com.juanjoseabuin.ualacitymobilechallenge.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
    primary = DarkBlue,
    onPrimary = DesertWhite,
    primaryContainer = LightBlue,
    onPrimaryContainer = DarkBlue,
    secondary = SandYellow,
    onSecondary = DarkBlue,
    secondaryContainer = SandYellow.copy(alpha = 0.6f),
    onSecondaryContainer = DarkBlue,
    tertiary = LightBlue,
    onTertiary = DarkBlue,
    background = DesertWhite,
    onBackground = DarkBlue,
    surface = DesertWhite,
    onSurface = DarkBlue,
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD4),
    onErrorContainer = Color(0xFF410002)
)

val DarkColorScheme = darkColorScheme(
    primary = LightBlue,
    onPrimary = DarkBlue,
    primaryContainer = DarkBlue,
    onPrimaryContainer = DesertWhite,
    secondary = SandYellow,
    onSecondary = DarkBlue,
    secondaryContainer = SandYellow.copy(alpha = 0.3f),
    onSecondaryContainer = DesertWhite,
    tertiary = LightBlue.copy(alpha = 0.7f),
    onTertiary = DarkBlue,
    background = DarkBlue,
    onBackground = DesertWhite,
    surface = DarkBlue,
    onSurface = DesertWhite,
    error = Color(0xFFCF6679),
    onError = Color.Black,
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFFDAD4)
)

@Composable
fun UalaCityMobileChallengeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
}
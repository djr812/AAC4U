package net.djrogers.aac4u.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ── Standard colour schemes ──

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = White,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue900,
    secondary = Green600,
    onSecondary = White,
    secondaryContainer = Green100,
    onSecondaryContainer = Green900,
    surface = White,
    onSurface = Grey900,
    surfaceVariant = Grey100,
    onSurfaceVariant = Grey700,
    error = Red600,
    onError = White
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue300,
    onPrimary = Blue900,
    primaryContainer = Blue800,
    onPrimaryContainer = Blue100,
    secondary = Green300,
    onSecondary = Green900,
    secondaryContainer = Green800,
    onSecondaryContainer = Green100,
    surface = Grey900,
    onSurface = Grey100,
    surfaceVariant = Grey800,
    onSurfaceVariant = Grey300,
    error = Red300,
    onError = Red900
)

// ── High contrast schemes (for users with vision issues) ──

private val HighContrastLightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = Yellow,
    primaryContainer = Yellow,
    onPrimaryContainer = Black,
    secondary = Black,
    onSecondary = White,
    surface = White,
    onSurface = Black,
    surfaceVariant = Yellow,
    onSurfaceVariant = Black,
    error = Red900,
    onError = White
)

private val HighContrastDarkColorScheme = darkColorScheme(
    primary = Yellow,
    onPrimary = Black,
    primaryContainer = Black,
    onPrimaryContainer = Yellow,
    secondary = White,
    onSecondary = Black,
    surface = Black,
    onSurface = Yellow,
    surfaceVariant = Grey900,
    onSurfaceVariant = Yellow,
    error = Red300,
    onError = Black
)

@Composable
fun AAC4UTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast && darkTheme -> HighContrastDarkColorScheme
        highContrast -> HighContrastLightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AAC4UTypography,
        shapes = AAC4UShapes,
        content = content
    )
}

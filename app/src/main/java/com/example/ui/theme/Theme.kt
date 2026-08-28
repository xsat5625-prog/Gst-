package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = Purple200,
    onPrimary = Purple800,
    primaryContainer = Purple700,
    onPrimaryContainer = Purple100,
    secondary = Purple300,
    onSecondary = Purple900,
    secondaryContainer = DarkSurfaceContainer,
    onSecondaryContainer = Purple100,
    tertiary = TertiaryRoseContainer,
    onTertiary = OnTertiaryRoseContainer,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    outlineVariant = DarkSurfaceContainer,
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple600, // #6750A4
    onPrimary = Color.White,
    primaryContainer = Purple100, // #EADDFF
    onPrimaryContainer = Purple900, // #21005D
    secondary = SecondaryPurple, // #625B71
    onSecondary = Color.White,
    secondaryContainer = SecondaryPurpleContainer, // #E8DEF8
    onSecondaryContainer = OnSecondaryPurpleContainer,
    tertiary = TertiaryRose, // #7D5260
    onTertiary = Color.White,
    tertiaryContainer = TertiaryRoseContainer,
    onTertiaryContainer = OnTertiaryRoseContainer,
    background = PolishBackground, // #FDF8FF
    onBackground = PolishTextPrimary, // #1C1B1F
    surface = PolishSurface,
    onSurface = PolishTextPrimary,
    surfaceVariant = PolishSurfaceVariant, // #F3EDF7
    onSurfaceVariant = PolishTextSecondary, // #49454F
    outline = PolishOutline, // #79747E
    outlineVariant = PolishOutlineVariant, // #CAC4D0
    error = PolishError, // #B3261E
    onError = Color.White,
    errorContainer = PolishErrorContainer,
    onErrorContainer = PolishOnErrorContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent tailored Professional Polish brand colors
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

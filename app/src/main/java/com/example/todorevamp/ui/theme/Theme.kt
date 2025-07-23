package com.example.todorevamp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPink80,
    secondary = DarkPinkGrey80,
    tertiary = DarkRose80,
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF2D2D2D),
    surfaceVariant = Color(0xFF3D3D3D),
    onPrimary = Color(0xFF2D0A1F),
    onSecondary = Color(0xFF3E2F33),
    onTertiary = Color(0xFF33111A),
    onBackground = Color(0xFFE8E8E8),
    onSurface = Color(0xFFE8E8E8),
    onSurfaceVariant = Color(0xFFB8B8B8),
    primaryContainer = Color(0xFF7B2D47),
    secondaryContainer = Color(0xFF5D4E52),
    tertiaryContainer = Color(0xFF6B2B3E),
    onPrimaryContainer = Color(0xFFFFD6E7),
    onSecondaryContainer = Color(0xFFE8DEE3),
    onTertiaryContainer = Color(0xFFFFB3C7)
)

private val LightColorScheme = lightColorScheme(
    primary = Pink40,
    secondary = PinkGrey40,
    tertiary = Rose40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    surfaceVariant = LightPink,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF49454F),
    primaryContainer = MediumPink,
    secondaryContainer = Color(0xFFE8DEE3),
    tertiaryContainer = Color(0xFFFFD6E7),
    onPrimaryContainer = DarkPink,
    onSecondaryContainer = Color(0xFF3E2F33),
    onTertiaryContainer = Color(0xFF33111A),
    errorContainer = Color(0xFFFFF0F0),
    onErrorContainer = Color(0xFFD32F2F)
)

@Composable
fun TodoRevampTheme(
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
package com.lightdarktools.passcrypt.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * PassCrypt Standardized Palette (3-Color Philosophy)
 * 1. Primary: Brand main blue (Vivid)
 * 2. Secondary: Supporting UI elements (Containers, chips)
 * 3. Tertiary (Accent): Highlights, icons, avatars
 */

private val DarkColorScheme = darkColorScheme(
    primary            = Color(0xFF9ECAFF), // primary blue
    onPrimary          = Color(0xFF003258),
    primaryContainer   = Color(0xFF00497B),
    onPrimaryContainer = Color(0xFFD1E4FF),

    secondary          = Color(0xFF448AFF), // secondary blue
    onSecondary        = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF004494),
    onSecondaryContainer = Color(0xFFD9E2FF),

    tertiary           = Color(0xFF82B1FF), // accent blue
    onTertiary         = Color(0xFF002F66),
    tertiaryContainer  = Color(0xFF004494),
    onTertiaryContainer = Color(0xFFD9E2FF),

    background         = Color(0xFF0A0C10),
    onBackground       = Color(0xFFE2E2E6),
    surface            = Color(0xFF12161E),
    onSurface          = Color(0xFFE2E2E6),
    surfaceVariant     = Color(0xFF1E2530),
    onSurfaceVariant   = Color(0xFFB9C3D6),
    outline            = Color(0xFF8D99B0),
    outlineVariant     = Color(0xFF3A4559),
    error              = Color(0xFFFFB4AB),
    onError            = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary            = Color(0xFF2979FF), // primary blue
    onPrimary          = Color(0xFFFFFFFF),
    primaryContainer   = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),

    secondary          = Color(0xFF1565C0), // secondary blue
    onSecondary        = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD1E4FF),
    onSecondaryContainer = Color(0xFF001D36),

    tertiary           = Color(0xFF448AFF), // accent blue
    onTertiary         = Color(0xFFFFFFFF),
    tertiaryContainer  = Color(0xFFD9E2FF),
    onTertiaryContainer = Color(0xFF001945),

    background         = Color(0xFFFAFBFF),
    onBackground       = Color(0xFF1A1C1E),
    surface            = Color(0xFFF0F4FF),
    onSurface          = Color(0xFF1A1C1E),
    surfaceVariant     = Color(0xFFE0E7F5),
    onSurfaceVariant   = Color(0xFF3A4559),
    outline            = Color(0xFF6B7A95),
    outlineVariant     = Color(0xFFC0CCDE),
    error              = Color(0xFFBA1A1A),
    onError            = Color(0xFFFFFFFF)
)

@Composable
fun PassCryptTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

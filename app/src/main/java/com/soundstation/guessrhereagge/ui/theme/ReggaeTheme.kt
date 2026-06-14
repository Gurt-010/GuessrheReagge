package com.soundstation.guessrhereagge.ui.theme

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat

@Immutable
data class ReggaeThemeColors(
    val backgroundCharcoal: Color = ReggaeColors.BackgroundCharcoal,
    val backgroundOlive: Color = ReggaeColors.BackgroundOlive,
    val primary: Color = ReggaeColors.LionGreen,
    val secondary: Color = ReggaeColors.SunshineGold,
    val accent: Color = ReggaeColors.FireRed,
    val textPrimary: Color = ReggaeColors.TextPrimary,
    val textGold: Color = ReggaeColors.TextGold,
    val surface: Color = ReggaeColors.SurfaceDark,
    val surfaceElevated: Color = ReggaeColors.SurfaceElevated,
)

val LocalReggaeColors = staticCompositionLocalOf { ReggaeThemeColors() }

object ReggaeTheme {
    val colors: ReggaeThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalReggaeColors.current
}

private val ReggaeMaterialScheme = darkColorScheme(
    primary = ReggaeColors.LionGreen,
    onPrimary = ReggaeColors.TextPrimary,
    primaryContainer = ReggaeColors.SurfaceElevated,
    onPrimaryContainer = ReggaeColors.TextPrimary,
    secondary = ReggaeColors.SunshineGold,
    onSecondary = ReggaeColors.BackgroundCharcoal,
    secondaryContainer = ReggaeColors.SurfaceDark,
    onSecondaryContainer = ReggaeColors.TextGold,
    tertiary = ReggaeColors.FireRed,
    onTertiary = ReggaeColors.TextPrimary,
    background = ReggaeColors.BackgroundCharcoal,
    onBackground = ReggaeColors.TextPrimary,
    surface = ReggaeColors.SurfaceDark,
    onSurface = ReggaeColors.TextPrimary,
    surfaceVariant = ReggaeColors.BackgroundOlive,
    onSurfaceVariant = ReggaeColors.TextGold,
    error = ReggaeColors.FireRed,
    onError = ReggaeColors.TextPrimary,
    outline = ReggaeColors.LionGreen,
)

@Composable
fun ReggaeTheme(content: @Composable () -> Unit) {
    val activity = LocalActivity.current
    androidx.compose.runtime.SideEffect {
        val window = activity?.window ?: return@SideEffect
        window.statusBarColor = ReggaeColors.BackgroundCharcoal.toArgb()
        window.navigationBarColor = ReggaeColors.BackgroundCharcoal.toArgb()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }

    CompositionLocalProvider(LocalReggaeColors provides ReggaeThemeColors()) {
        MaterialTheme(
            colorScheme = ReggaeMaterialScheme,
            typography = ReggaeTypography,
            content = content,
        )
    }
}

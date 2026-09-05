package com.internshield.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CyberColorScheme = darkColorScheme(
    primary = AccentOrange,
    onPrimary = Color.White,
    primaryContainer = AccentOrangeBg,
    onPrimaryContainer = AccentOrangeBright,
    secondary = SuccessNeonGreen,
    onSecondary = Color(0xFF08090D),
    secondaryContainer = CardElevated,
    onSecondaryContainer = AccentOrangeBright,
    background = BgDark,
    onBackground = TextWhite,
    surface = CardSurface,
    onSurface = TextWhite,
    surfaceVariant = CardElevated,
    onSurfaceVariant = TextMutedGray,
    outline = CardBorder,
    outlineVariant = CardBorderSubtle,
    error = DangerPinkRed,
    onError = Color.White,
    errorContainer = DangerRedDark,
    onErrorContainer = DangerPinkRed
)

@Composable
fun InternShieldTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = CyberColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgDark.toArgb()
            window.navigationBarColor = BgDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.bunkmarte.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Dark color scheme — sleek black & neon purple aesthetic.
 * Pure black backgrounds with elevated dark cards and vibrant purple accents.
 */
private val BunkMarteDarkScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = PureBlack,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = OnPurpleContainer,
    secondary = DeepViolet,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFF1E0A3C),
    onSecondaryContainer = Color(0xFFD4BBFF),
    background = PureBlack,
    onBackground = PureWhite,
    surface = ElevatedSurface,
    onSurface = PureWhite,
    surfaceVariant = CardDark,
    onSurfaceVariant = LightGrayText,
    error = NeonRed,
    onError = PureBlack,
    outline = DimGray,
    outlineVariant = Color(0xFF252535),
    inverseSurface = PureWhite,
    inverseOnSurface = PureBlack,
    surfaceTint = NeonPurple
)

@Composable
fun BunkMarteTheme(content: @Composable () -> Unit) {
    val colorScheme = BunkMarteDarkScheme

    // Make system bars match the dark theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PureBlack.toArgb()
            window.navigationBarColor = ElevatedSurface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BunkMarteTypography,
        content = content
    )
}

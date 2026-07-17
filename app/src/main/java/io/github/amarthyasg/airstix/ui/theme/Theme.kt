package io.github.amarthyasg.airstix.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import io.github.amarthyasg.airstix.data.MinimalistPalette
import androidx.compose.material3.ColorScheme as MaterialColorScheme

/**
 * Creates a custom minimalist color palette mapping PRD roles directly to MaterialColorScheme roles.
 */
private fun createMinimalistColorPalette(palette: MinimalistPalette): MaterialColorScheme {
    val errorColor = if (palette.isLight) Color(0xFFB3261E) else Color(0xFFFF5252)
    return darkColorScheme(
        primary = palette.accent,
        onPrimary = contrasting(palette.accent),
        secondary = palette.muted,
        onSecondary = palette.text,
        tertiary = palette.muted,
        onTertiary = palette.text,
        background = palette.background,
        onBackground = palette.text,
        surface = palette.surface,
        onSurface = palette.text,
        onSurfaceVariant = palette.text,
        outline = palette.muted,
        outlineVariant = palette.muted,
        error = errorColor,
        onError = contrasting(errorColor),
        primaryContainer = darken(palette.accent, 0.6f),
        secondaryContainer = darken(palette.muted, 0.6f),
        onPrimaryContainer = lighten(palette.accent, 0.6f),
        onSecondaryContainer = lighten(palette.muted, 0.6f)
    ).copy(
        surfaceContainer = palette.surface,
        surfaceContainerLow = palette.surface,
        surfaceContainerHigh = palette.surface,
        surfaceContainerHighest = palette.surface,
        surfaceContainerLowest = palette.surface,
        surfaceDim = palette.surface,
        surfaceBright = palette.surface
    )
}

@Composable
fun VirtualGamePadMobileTheme(
    minimalistPalette: MinimalistPalette = MinimalistPalette.CRIMSON_ARCADE,
    content: @Composable () -> Unit
) {
    val colorScheme = createMinimalistColorPalette(minimalistPalette)

    val view = LocalView.current
    if (!view.isInEditMode) {
        val isLight = minimalistPalette.isLight
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = isLight
            insetsController.isAppearanceLightNavigationBars = isLight
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

package com.mohamedadel.mytube.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ActionDark,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF3A1A1C),
    onPrimaryContainer = Color(0xFFFFDAD7),
    secondary = VerifiedOnDark,
    onSecondary = Color(0xFF0F0F12),
    tertiary = SignalOnDark,
    onTertiary = Color(0xFFFFFFFF),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    // D6 (audit P1 2026-11-11): explicit ramps (were M3 defaults).
    surfaceContainerLowest = DarkContainerLowest,
    surfaceContainerLow = DarkContainerLow,
    surfaceContainer = DarkContainer,
    surfaceContainerHigh = DarkContainerHigh,
    surfaceContainerHighest = DarkContainerHighest,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = ActionLight,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD7),
    onPrimaryContainer = Color(0xFF410002),
    secondary = VerifiedOnLight,
    onSecondary = Color(0xFFFFFFFF),
    tertiary = ActionLight,
    onTertiary = Color(0xFFFFFFFF),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    // D6 (audit P1 2026-11-11): explicit ramps (were M3 defaults).
    surfaceContainerLowest = LightContainerLowest,
    surfaceContainerLow = LightContainerLow,
    surfaceContainer = LightContainer,
    surfaceContainerHigh = LightContainerHigh,
    surfaceContainerHighest = LightContainerHighest,
    outline = LightOutline
)

// One shape scale for the whole app (M3 Expressive-style): cards 16, player 20/24, search 28.
val MyTubeShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyTubeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    black: Boolean = false,
    accent: String = "red",
    rtl: Boolean = true,
    // v0.4.6 senior mode: scale text only (dp layout untouched).
    senior: Boolean = false,
    content: @Composable () -> Unit
) {
    val dark = darkTheme || black
    val fill = AccentFills[accent] ?: AccentRed
    val containers = if (dark) AccentContainersDark else AccentContainersLight
    val (container, onContainer) = containers[accent] ?: containers["red"]!!
    val base = if (dark) DarkColorScheme else LightColorScheme
    val colorScheme = if (black) {
        darkColorScheme(
            primary = fill,
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = container,
            onPrimaryContainer = onContainer,
            secondary = VerifiedOnDark,
            onSecondary = Color(0xFF0F0F12),
            tertiary = SignalOnDark,
            onTertiary = Color(0xFFFFFFFF),
            background = BlackBackground,
            onBackground = DarkOnSurface,
            surface = BlackSurface,
            onSurface = DarkOnSurface,
            surfaceVariant = BlackSurfaceVariant,
            onSurfaceVariant = DarkOnSurfaceVariant,
            // D6 (audit P1 2026-11-11): near-black ramps (pure black crushes hierarchy).
            surfaceContainerLowest = BlackContainerLowest,
            surfaceContainerLow = BlackContainerLow,
            surfaceContainer = BlackContainer,
            surfaceContainerHigh = BlackContainerHigh,
            surfaceContainerHighest = BlackContainerHighest,
            outline = BlackOutline
        )
    } else if (accent == "red") {
        base
    } else {
        base.copy(primary = fill, onPrimary = Color(0xFFFFFFFF), primaryContainer = container, onPrimaryContainer = onContainer)
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
        }
    }
    // Arabic UI forces RTL; English UI uses LTR. Direction follows the ui-language pref.
    // Senior mode bumps font scale without touching dp geometry (safe for all layouts).
    val density = LocalDensity.current
    val seniorDensity = if (senior) androidx.compose.ui.unit.Density(density.density, 1.3f) else density
    CompositionLocalProvider(
        LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
        LocalDensity provides seniorDensity
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = MyTubeTypography, shapes = MyTubeShapes, content = content)
    }
}
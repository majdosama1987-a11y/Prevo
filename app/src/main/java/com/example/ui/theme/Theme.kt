package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = TechBlue,
    onPrimary = PureWhite,
    primaryContainer = TechBlueContainer,
    onPrimaryContainer = DeepSlate,
    secondary = HardwareAmber,
    onSecondary = PureWhite,
    secondaryContainer = HardwareAmberContainer,
    onSecondaryContainer = DeepSlate,
    tertiary = IndigoAccent,
    onTertiary = PureWhite,
    tertiaryContainer = IndigoContainer,
    onTertiaryContainer = DeepSlate,
    background = AlabasterBg,
    onBackground = DeepSlate,
    surface = PureWhite,
    onSurface = DeepSlate,
    surfaceVariant = Slate200,
    onSurfaceVariant = Slate700,
    outline = Slate300,
    outlineVariant = Slate200,
    error = RoseError,
    onError = PureWhite,
    errorContainer = RoseContainer,
    onErrorContainer = DeepSlate
)

private val DarkColorScheme = darkColorScheme(
    primary = TechBlueLight,
    onPrimary = DeepSlate,
    primaryContainer = TechBlueDarkContainer,
    onPrimaryContainer = TechBlueContainer,
    secondary = HardwareAmberLight,
    onSecondary = DeepSlate,
    secondaryContainer = HardwareAmberDarkContainer,
    onSecondaryContainer = HardwareAmberContainer,
    tertiary = IndigoContainer,
    onTertiary = DeepSlate,
    background = ObsidianBg,
    onBackground = PureWhite,
    surface = Slate800,
    onSurface = PureWhite,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate300,
    outline = Slate500,
    outlineVariant = Slate700,
    error = RoseError,
    onError = PureWhite,
    errorContainer = RoseContainer,
    onErrorContainer = DeepSlate
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MobilePartsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Geometric Balance highly-rounded, structured corners
val GeometricShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),       // Matches HTML rounded-[24px]
    extraLarge = RoundedCornerShape(28.dp)   // Matches HTML rounded-[28px]
)

private val LightColorScheme = lightColorScheme(
    primary = NeonRuby,
    secondary = NeonCyan,
    tertiary = ElectricViolet,
    background = MidnightBlack,
    surface = CosmicSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = OnSpaceWhite,
    onSurface = OnSpaceWhite,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = OnSpaceWhite,
    outline = DividerGray
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Force our custom high-fidelity theme for brand alignment
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        shapes = GeometricShapes,
        typography = Typography,
        content = content
    )
}

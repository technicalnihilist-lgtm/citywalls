package com.citywalls.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CityWallsShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

/**
 * CityWalls is dark only by design. [pureBlack] switches the canvas between
 * true AMOLED black and a deep charcoal, which is the one appearance choice
 * the settings screen exposes.
 */
@Composable
fun CityWallsTheme(
    pureBlack: Boolean = true,
    content: @Composable () -> Unit
) {
    val background = if (pureBlack) AmoledBlack else DeepCharcoal
    val colorScheme = darkColorScheme(
        primary = Champagne,
        onPrimary = InkBlack,
        primaryContainer = SurfaceElevated,
        onPrimaryContainer = Champagne,
        secondary = ChampagneDeep,
        onSecondary = InkBlack,
        tertiary = ChampagneMuted,
        background = background,
        onBackground = TextPrimary,
        surface = SurfaceCharcoal,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceElevated,
        onSurfaceVariant = TextSecondary,
        outline = GlassBorderStrong,
        outlineVariant = GlassBorder,
        error = ErrorRose,
        onError = InkBlack
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = CityWallsTypography,
        shapes = CityWallsShapes,
        content = content
    )
}

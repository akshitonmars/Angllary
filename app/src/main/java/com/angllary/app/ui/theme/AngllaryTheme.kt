package com.angllary.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private object Palette {
    val Canvas = Color(0xFFF6F5F2)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceSoft = Color(0xFFEDEBE6)
    val Ink = Color(0xFF20211F)
    val Muted = Color(0xFF85847F)
    val Accent = Color(0xFFC86F4A)
    val Divider = Color(0xFFE8E5DF)

    val CanvasDark = Color(0xFF121311)
    val SurfaceDark = Color(0xFF1C1E1B)
    val SurfaceSoftDark = Color(0xFF282A26)
    val InkDark = Color(0xFFF3F1EC)
    val MutedDark = Color(0xFFA5A39C)
    val AccentDark = Color(0xFFE59A78)
    val DividerDark = Color(0xFF33352F)
}

private val AngllaryTypography = Typography(
    headlineMedium = TextStyle(
        fontSize = 26.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.6).sp
    ),
    titleLarge = TextStyle(
        fontSize = 19.sp,
        lineHeight = 25.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.25).sp
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
    labelLarge = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
)

private val LightColors = lightColorScheme(
    primary = Palette.Accent,
    onPrimary = Palette.Surface,
    secondary = Palette.Ink,
    onSecondary = Palette.Surface,
    background = Palette.Canvas,
    onBackground = Palette.Ink,
    surface = Palette.Surface,
    onSurface = Palette.Ink,
    surfaceVariant = Palette.SurfaceSoft,
    onSurfaceVariant = Palette.Muted,
    outline = Palette.Divider
)

private val DarkColors = darkColorScheme(
    primary = Palette.AccentDark,
    onPrimary = Palette.CanvasDark,
    secondary = Palette.InkDark,
    onSecondary = Palette.CanvasDark,
    background = Palette.CanvasDark,
    onBackground = Palette.InkDark,
    surface = Palette.SurfaceDark,
    onSurface = Palette.InkDark,
    surfaceVariant = Palette.SurfaceSoftDark,
    onSurfaceVariant = Palette.MutedDark,
    outline = Palette.DividerDark
)

@Composable
fun AngllaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AngllaryTypography,
        content = content
    )
}

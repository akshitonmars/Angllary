package com.angllary.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Angllary uses a deliberately neutral palette.
 *
 * Every neutral token has identical RGB channels. This prevents subtle teal,
 * green or warm colour casts from appearing in surfaces and text.
 */
private object Palette {
    val Canvas = Color(0xFFFFFFFF)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceLow = Color(0xFFFFFFFF)
    val SurfaceHigh = Color(0xFFEAEAEA)
    val Ink = Color(0xFF000000)
    val InkSoft = Color(0xFF333333)
    val Muted = Color(0xFF666666)
    val Divider = Color(0xFFD6D6D6)

    val CanvasDark = Color(0xFF000000)
    val SurfaceDark = Color(0xFF111111)
    val SurfaceLowDark = Color(0xFF191919)
    val SurfaceHighDark = Color(0xFF242424)
    val InkDark = Color(0xFFF4F4F4)
    val InkSoftDark = Color(0xFFE5E5E5)
    val MutedDark = Color(0xFFB3B3B3)
    val DividerDark = Color(0xFF363636)

    val Error = Color(0xFF666666)
}

private val AngllaryFont = FontFamily(Font(com.angllary.app.R.font.google_sans_flex))

private val LightColors = lightColorScheme(
    primary = Palette.Ink,
    onPrimary = Palette.Surface,
    primaryContainer = Palette.SurfaceHigh,
    onPrimaryContainer = Palette.Ink,
    secondary = Palette.InkSoft,
    onSecondary = Palette.Surface,
    background = Palette.Canvas,
    onBackground = Palette.Ink,
    surface = Palette.Surface,
    onSurface = Palette.Ink,
    surfaceVariant = Palette.SurfaceLow,
    onSurfaceVariant = Palette.Muted,
    surfaceDim = Palette.SurfaceLow,
    surfaceBright = Palette.Surface,
    surfaceContainerLowest = Palette.Surface,
    surfaceContainerLow = Palette.Canvas,
    surfaceContainer = Palette.SurfaceLow,
    surfaceContainerHigh = Palette.SurfaceHigh,
    surfaceContainerHighest = Color(0xFFE0E0E0),
    outline = Palette.Divider,
    outlineVariant = Palette.Divider,
    error = Palette.Error,
    onError = Palette.Surface
)

private val DarkColors = darkColorScheme(
    primary = Palette.InkDark,
    onPrimary = Palette.CanvasDark,
    primaryContainer = Palette.SurfaceHighDark,
    onPrimaryContainer = Palette.InkDark,
    secondary = Palette.InkSoftDark,
    onSecondary = Palette.CanvasDark,
    background = Palette.CanvasDark,
    onBackground = Palette.InkDark,
    surface = Palette.SurfaceDark,
    onSurface = Palette.InkDark,
    surfaceVariant = Palette.SurfaceLowDark,
    onSurfaceVariant = Palette.MutedDark,
    surfaceDim = Palette.CanvasDark,
    surfaceBright = Palette.SurfaceHighDark,
    surfaceContainerLowest = Color(0xFF0B0B0B),
    surfaceContainerLow = Palette.SurfaceDark,
    surfaceContainer = Palette.SurfaceLowDark,
    surfaceContainerHigh = Palette.SurfaceHighDark,
    surfaceContainerHighest = Color(0xFF303030),
    outline = Palette.DividerDark,
    outlineVariant = Palette.DividerDark,
    error = Palette.MutedDark,
    onError = Palette.CanvasDark
)

private val AngllaryTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = AngllaryFont, fontWeight = FontWeight.Bold, letterSpacing = (-1.2).sp),
        displayMedium = base.displayMedium.copy(fontFamily = AngllaryFont, fontWeight = FontWeight.Bold, letterSpacing = (-0.7).sp),
        displaySmall = base.displaySmall.copy(fontFamily = AngllaryFont, fontWeight = FontWeight.SemiBold),
        headlineLarge = base.headlineLarge.copy(fontFamily = AngllaryFont, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.65).sp),
        headlineMedium = base.headlineMedium.copy(fontFamily = AngllaryFont, fontSize = 25.sp, lineHeight = 31.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.45).sp),
        headlineSmall = base.headlineSmall.copy(fontFamily = AngllaryFont, fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.25).sp),
        titleLarge = base.titleLarge.copy(fontFamily = AngllaryFont, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontFamily = AngllaryFont, fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontFamily = AngllaryFont, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(fontFamily = AngllaryFont, fontSize = 15.sp, lineHeight = 22.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = AngllaryFont, fontSize = 13.sp, lineHeight = 19.sp),
        bodySmall = base.bodySmall.copy(fontFamily = AngllaryFont, fontSize = 12.sp, lineHeight = 17.sp),
        labelLarge = base.labelLarge.copy(fontFamily = AngllaryFont, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
        labelMedium = base.labelMedium.copy(fontFamily = AngllaryFont, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
        labelSmall = base.labelSmall.copy(fontFamily = AngllaryFont, fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.25.sp)
    )
}

private val AngllaryShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(18.dp)
)

data class ExtendedColors(
    val viewerBackground: Color,
    val viewerSurface: Color,
    val viewerOnSurface: Color,
    val viewerMuted: Color,
    val scrim: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        viewerBackground = Color(0xFF000000),
        viewerSurface = Color(0xFF171717),
        viewerOnSurface = Color(0xFFF4F4F4),
        viewerMuted = Color(0xFFB0B0B0),
        scrim = Color(0x99000000)
    )
}

private val LightExtended = ExtendedColors(
    viewerBackground = Color(0xFF101010),
    viewerSurface = Color(0xFF242424),
    viewerOnSurface = Color(0xFFF4F4F4),
    viewerMuted = Color(0xFFB0B0B0),
    scrim = Color(0x66000000)
)

private val DarkExtended = ExtendedColors(
    viewerBackground = Color(0xFF000000),
    viewerSurface = Color(0xFF171717),
    viewerOnSurface = Color(0xFFF4F4F4),
    viewerMuted = Color(0xFFA3A3A3),
    scrim = Color(0x99000000)
)

@Composable
fun AngllaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalExtendedColors provides if (darkTheme) DarkExtended else LightExtended
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AngllaryTypography,
            shapes = AngllaryShapes,
            content = content
        )
    }
}

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

private object Palette {
    val Canvas = Color(0xFFF2F1ED)
    val Surface = Color(0xFFFAF9F6)
    val SurfaceDim = Color(0xFFE8E7E2)
    val Ink = Color(0xFF242320)
    val InkSoft = Color(0xFF484741)
    val Muted = Color(0xFF77766F)
    val Divider = Color(0xFFD9D8D2)

    val CanvasDark = Color(0xFF151514)
    val SurfaceDark = Color(0xFF20201E)
    val SurfaceDimDark = Color(0xFF2B2B29)
    val InkDark = Color(0xFFF0EFEA)
    val InkSoftDark = Color(0xFFD0CFC8)
    val MutedDark = Color(0xFFA6A59F)
    val DividerDark = Color(0xFF393936)

    val Error = Color(0xFF68645F)
}

private val GoogleSans = FontFamily(Font(com.angllary.app.R.font.google_sans_flex))

private val LightColors = lightColorScheme(
    primary = Palette.Ink,
    onPrimary = Palette.Surface,
    primaryContainer = Palette.SurfaceDim,
    onPrimaryContainer = Palette.Ink,
    secondary = Palette.InkSoft,
    onSecondary = Palette.Surface,
    background = Palette.Canvas,
    onBackground = Palette.Ink,
    surface = Palette.Surface,
    onSurface = Palette.Ink,
    surfaceVariant = Palette.SurfaceDim,
    onSurfaceVariant = Palette.Muted,
    surfaceDim = Palette.SurfaceDim,
    outline = Palette.Divider,
    outlineVariant = Palette.Divider,
    error = Palette.Error,
    onError = Palette.Surface
)

private val DarkColors = darkColorScheme(
    primary = Palette.InkDark,
    onPrimary = Palette.CanvasDark,
    primaryContainer = Palette.SurfaceDimDark,
    onPrimaryContainer = Palette.InkDark,
    secondary = Palette.InkSoftDark,
    onSecondary = Palette.CanvasDark,
    background = Palette.CanvasDark,
    onBackground = Palette.InkDark,
    surface = Palette.SurfaceDark,
    onSurface = Palette.InkDark,
    surfaceVariant = Palette.SurfaceDimDark,
    onSurfaceVariant = Palette.MutedDark,
    surfaceDim = Palette.SurfaceDimDark,
    outline = Palette.DividerDark,
    outlineVariant = Palette.DividerDark,
    error = Palette.MutedDark,
    onError = Palette.CanvasDark
)

private val AngllaryTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = GoogleSans, fontWeight = FontWeight.Bold, letterSpacing = (-1.0).sp),
        displayMedium = base.displayMedium.copy(fontFamily = GoogleSans, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        displaySmall = base.displaySmall.copy(fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold),
        headlineLarge = base.headlineLarge.copy(fontFamily = GoogleSans, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineMedium = base.headlineMedium.copy(fontFamily = GoogleSans, fontSize = 25.sp, lineHeight = 31.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.45).sp),
        headlineSmall = base.headlineSmall.copy(fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontFamily = GoogleSans, fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp),
        titleMedium = base.titleMedium.copy(fontFamily = GoogleSans, fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontFamily = GoogleSans, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(fontFamily = GoogleSans, fontSize = 15.sp, lineHeight = 22.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = GoogleSans, fontSize = 13.sp, lineHeight = 19.sp),
        bodySmall = base.bodySmall.copy(fontFamily = GoogleSans, fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge = base.labelLarge.copy(fontFamily = GoogleSans, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
        labelMedium = base.labelMedium.copy(fontFamily = GoogleSans, fontSize = 12.sp, fontWeight = FontWeight.Medium),
        labelSmall = base.labelSmall.copy(fontFamily = GoogleSans, fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.35.sp)
    )
}

private val AngllaryShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
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
        viewerBackground = Color(0xFF171715),
        viewerSurface = Color(0xFF262624),
        viewerOnSurface = Color(0xFFF0EFEA),
        viewerMuted = Color(0xFFB0AFA8),
        scrim = Color(0x99000000)
    )
}

private val LightExtended = ExtendedColors(
    viewerBackground = Color(0xFF191918),
    viewerSurface = Color(0xFF292927),
    viewerOnSurface = Color(0xFFF2F1ED),
    viewerMuted = Color(0xFFB8B7B0),
    scrim = Color(0x66000000)
)

private val DarkExtended = ExtendedColors(
    viewerBackground = Color(0xFF111110),
    viewerSurface = Color(0xFF242422),
    viewerOnSurface = Color(0xFFF0EFEA),
    viewerMuted = Color(0xFFA6A59F),
    scrim = Color(0x99000000)
)

@Composable
fun AngllaryTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
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

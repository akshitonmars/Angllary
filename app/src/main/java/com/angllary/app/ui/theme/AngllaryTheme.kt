package com.angllary.app.ui.theme

import android.graphics.Typeface
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private object Palette {
    val Canvas = Color(0xFFF2F2F0)
    val Surface = Color(0xFFFAFAF8)
    val SurfaceSoft = Color(0xFFE8E8E6)
    val Ink = Color(0xFF191919)
    val Muted = Color(0xFF747474)
    val Divider = Color(0xFFD9D9D6)
    val CanvasDark = Color(0xFF111111)
    val SurfaceDark = Color(0xFF1A1A1A)
    val SurfaceSoftDark = Color(0xFF252525)
    val InkDark = Color(0xFFF1F1EF)
    val MutedDark = Color(0xFFA5A5A5)
    val DividerDark = Color(0xFF353535)
}

// Uses Google Sans when it is installed on the device, with Android sans-serif fallback.
private val GoogleSans = FontFamily(Typeface.create("Google Sans", Typeface.NORMAL))

private val AngllaryTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = GoogleSans),
        displayMedium = base.displayMedium.copy(fontFamily = GoogleSans),
        displaySmall = base.displaySmall.copy(fontFamily = GoogleSans),
        headlineLarge = base.headlineLarge.copy(fontFamily = GoogleSans),
        headlineMedium = base.headlineMedium.copy(fontFamily = GoogleSans, fontSize = 25.sp, lineHeight = 31.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.55).sp),
        headlineSmall = base.headlineSmall.copy(fontFamily = GoogleSans),
        titleLarge = base.titleLarge.copy(fontFamily = GoogleSans, fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.25).sp),
        titleMedium = base.titleMedium.copy(fontFamily = GoogleSans, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontFamily = GoogleSans),
        bodyLarge = base.bodyLarge.copy(fontFamily = GoogleSans, fontSize = 15.sp, lineHeight = 22.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = GoogleSans, fontSize = 13.sp, lineHeight = 19.sp),
        bodySmall = base.bodySmall.copy(fontFamily = GoogleSans),
        labelLarge = base.labelLarge.copy(fontFamily = GoogleSans, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
        labelMedium = base.labelMedium.copy(fontFamily = GoogleSans),
        labelSmall = base.labelSmall.copy(fontFamily = GoogleSans)
    )
}

private val LightColors = lightColorScheme(
    primary = Palette.Ink, onPrimary = Palette.Surface,
    secondary = Palette.Ink, onSecondary = Palette.Surface,
    background = Palette.Canvas, onBackground = Palette.Ink,
    surface = Palette.Surface, onSurface = Palette.Ink,
    surfaceVariant = Palette.SurfaceSoft, onSurfaceVariant = Palette.Muted,
    outline = Palette.Divider
)

private val DarkColors = darkColorScheme(
    primary = Palette.InkDark, onPrimary = Palette.CanvasDark,
    secondary = Palette.InkDark, onSecondary = Palette.CanvasDark,
    background = Palette.CanvasDark, onBackground = Palette.InkDark,
    surface = Palette.SurfaceDark, onSurface = Palette.InkDark,
    surfaceVariant = Palette.SurfaceSoftDark, onSurfaceVariant = Palette.MutedDark,
    outline = Palette.DividerDark
)

@Composable
fun AngllaryTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AngllaryTypography,
        content = content
    )
}

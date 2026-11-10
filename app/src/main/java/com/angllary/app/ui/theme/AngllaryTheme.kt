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

/* ── Colour Palette ────────────────────────────────────────────── */

private object Palette {
    // Light
    val Canvas      = Color(0xFFF6F5F1)
    val Surface     = Color(0xFFFFFFFF)
    val SurfaceDim  = Color(0xFFEDECE8)
    val Ink         = Color(0xFF1A1A1A)
    val InkSoft     = Color(0xFF3D3D3D)
    val Muted       = Color(0xFF6E6E6E)
    val Divider     = Color(0xFFDDDDD8)
    val Accent      = Color(0xFF2D6B4F)   // Forest green – distinctive, elegant
    val AccentSoft  = Color(0xFFD9EDE4)
    val Error       = Color(0xFFBF3B3B)

    // Dark
    val CanvasD     = Color(0xFF0F0F0F)
    val SurfaceD    = Color(0xFF1B1B1B)
    val SurfaceDimD = Color(0xFF252525)
    val InkD        = Color(0xFFF0EFEB)
    val InkSoftD    = Color(0xFFCDCDC8)
    val MutedD      = Color(0xFF9A9A94)
    val DividerD    = Color(0xFF333332)
    val AccentD     = Color(0xFF6FC9A0)   // Brighter green for dark
    val AccentSoftD = Color(0xFF1E3A2D)
    val ErrorD      = Color(0xFFFF7878)
}

/* ── Colour Schemes ────────────────────────────────────────────── */

private val LightColors = lightColorScheme(
    primary           = Palette.Accent,
    onPrimary         = Color.White,
    primaryContainer  = Palette.AccentSoft,
    onPrimaryContainer = Palette.Accent,
    secondary         = Palette.InkSoft,
    onSecondary       = Color.White,
    background        = Palette.Canvas,
    onBackground      = Palette.Ink,
    surface           = Palette.Surface,
    onSurface         = Palette.Ink,
    surfaceVariant    = Palette.SurfaceDim,
    onSurfaceVariant  = Palette.Muted,
    surfaceDim        = Palette.SurfaceDim,
    outline           = Palette.Divider,
    outlineVariant    = Palette.Divider,
    error             = Palette.Error,
    onError           = Color.White
)

private val DarkColors = darkColorScheme(
    primary           = Palette.AccentD,
    onPrimary         = Palette.CanvasD,
    primaryContainer  = Palette.AccentSoftD,
    onPrimaryContainer = Palette.AccentD,
    secondary         = Palette.InkSoftD,
    onSecondary       = Palette.CanvasD,
    background        = Palette.CanvasD,
    onBackground      = Palette.InkD,
    surface           = Palette.SurfaceD,
    onSurface         = Palette.InkD,
    surfaceVariant    = Palette.SurfaceDimD,
    onSurfaceVariant  = Palette.MutedD,
    surfaceDim        = Palette.SurfaceDimD,
    outline           = Palette.DividerD,
    outlineVariant    = Palette.DividerD,
    error             = Palette.ErrorD,
    onError           = Palette.CanvasD
)

/* ── Typography ────────────────────────────────────────────────── */

// Google Sans Flex – loaded via downloadable-font provider; sans-serif is fallback.
private val GoogleSans = FontFamily(Font(com.angllary.app.R.font.google_sans_flex))

private val AngllaryTypography = Typography().let { base ->
    base.copy(
        displayLarge  = base.displayLarge.copy(fontFamily = GoogleSans, fontWeight = FontWeight.Bold, letterSpacing = (-1.0).sp),
        displayMedium = base.displayMedium.copy(fontFamily = GoogleSans, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        displaySmall  = base.displaySmall.copy(fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold),
        headlineLarge = base.headlineLarge.copy(fontFamily = GoogleSans, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineMedium = base.headlineMedium.copy(fontFamily = GoogleSans, fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp),
        headlineSmall = base.headlineSmall.copy(fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold),
        titleLarge    = base.titleLarge.copy(fontFamily = GoogleSans, fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp),
        titleMedium   = base.titleMedium.copy(fontFamily = GoogleSans, fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
        titleSmall    = base.titleSmall.copy(fontFamily = GoogleSans, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
        bodyLarge     = base.bodyLarge.copy(fontFamily = GoogleSans, fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp),
        bodyMedium    = base.bodyMedium.copy(fontFamily = GoogleSans, fontSize = 13.sp, lineHeight = 19.sp, letterSpacing = 0.15.sp),
        bodySmall     = base.bodySmall.copy(fontFamily = GoogleSans, fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge    = base.labelLarge.copy(fontFamily = GoogleSans, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
        labelMedium   = base.labelMedium.copy(fontFamily = GoogleSans, fontSize = 12.sp, fontWeight = FontWeight.Medium),
        labelSmall    = base.labelSmall.copy(fontFamily = GoogleSans, fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
    )
}

/* ── Shapes ────────────────────────────────────────────────────── */

private val AngllaryShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small      = RoundedCornerShape(10.dp),
    medium     = RoundedCornerShape(14.dp),
    large      = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/* ── Extended colours exposed via CompositionLocal ─────────────── */

data class ExtendedColors(
    val viewerBackground: Color,
    val viewerSurface: Color,
    val viewerOnSurface: Color,
    val viewerMuted: Color,
    val shimmer: Color,
    val scrim: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        viewerBackground = Color(0xFF0A0A0A),
        viewerSurface    = Color(0xFF1C1C1C),
        viewerOnSurface  = Color(0xFFF0EFEB),
        viewerMuted      = Color(0xFFAAAAAA),
        shimmer          = Color(0xFFE0E0DC),
        scrim            = Color(0x99000000)
    )
}

private val LightExtended = ExtendedColors(
    viewerBackground = Color(0xFF0A0A0A),
    viewerSurface    = Color(0xFF1E1E1E),
    viewerOnSurface  = Color(0xFFF0EFEB),
    viewerMuted      = Color(0xFFAAAAAA),
    shimmer          = Color(0xFFE4E4DF),
    scrim            = Color(0x66000000)
)

private val DarkExtended = ExtendedColors(
    viewerBackground = Color(0xFF070707),
    viewerSurface    = Color(0xFF181818),
    viewerOnSurface  = Color(0xFFF0EFEB),
    viewerMuted      = Color(0xFF888888),
    shimmer          = Color(0xFF2A2A28),
    scrim            = Color(0x99000000)
)

/* ── Theme Composable ──────────────────────────────────────────── */

@Composable
fun AngllaryTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors   = if (darkTheme) DarkColors else LightColors
    val extended = if (darkTheme) DarkExtended else LightExtended

    CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = colors,
            typography  = AngllaryTypography,
            shapes      = AngllaryShapes,
            content     = content
        )
    }
}

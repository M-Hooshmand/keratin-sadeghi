package com.keratinsadeghi.salon.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keratinsadeghi.salon.R

object SalonColors {
    val Ivory = Color(0xFFFBF4E6)
    val Cream = Color(0xFFFFFCF5)
    val Ink = Color(0xFF3E3022)
    val Muted = Color(0xFF8A765C)
    val Rose = Color(0xFFC9A24B)
    val RoseDeep = Color(0xFF7A5624)
    val Hot = Color(0xFFA67C2E)
    val Bubble = Color(0xFFE8D3A8)
    val Blush = Color(0xFFF6E8CE)
    val Line = Color(0xFFE8D8BB)
    val Lilac = Color(0xFFF3EADB)
    val Mint = Color(0xFFEAF0E1)
    val MintDeep = Color(0xFF4D6B3A)
    val Danger = Color(0xFF9B3B3B)
    val DangerBg = Color(0xFFFBE9E7)
}

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

private val Colors = lightColorScheme(
    primary = SalonColors.Hot,
    onPrimary = Color.White,
    primaryContainer = SalonColors.Blush,
    onPrimaryContainer = SalonColors.RoseDeep,
    secondary = SalonColors.Rose,
    onSecondary = Color.White,
    secondaryContainer = SalonColors.Lilac,
    onSecondaryContainer = SalonColors.Ink,
    tertiary = SalonColors.RoseDeep,
    onTertiary = Color.White,
    background = SalonColors.Ivory,
    onBackground = SalonColors.Ink,
    surface = Color.White,
    onSurface = SalonColors.Ink,
    surfaceVariant = SalonColors.Blush,
    onSurfaceVariant = SalonColors.Muted,
    outline = SalonColors.Line,
    error = SalonColors.Danger,
    onError = Color.White,
    errorContainer = SalonColors.DangerBg,
    onErrorContainer = SalonColors.Danger,
)

private val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = Vazirmatn,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Vazirmatn,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 34.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Vazirmatn,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Vazirmatn,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Vazirmatn,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 23.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Vazirmatn,
        fontSize = 15.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Vazirmatn,
        fontSize = 13.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Vazirmatn,
        fontSize = 11.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Vazirmatn,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Vazirmatn,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
    ),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

@Composable
fun KeratinTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = Colors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

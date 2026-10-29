package com.keratinsadeghi.salon.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keratinsadeghi.salon.R

object SalonColors {
    val Ivory = Color(0xFFFFF4FA)
    val Cream = Color(0xFFFFFAFD)
    val Ink = Color(0xFF3B2030)
    val Muted = Color(0xFFA07891)
    val Rose = Color(0xFFF06FA8)
    val RoseDeep = Color(0xFFA82A66)
    val Hot = Color(0xFFFF2C92)
    val Bubble = Color(0xFFFF8FC4)
    val Blush = Color(0xFFFFE3F1)
    val Line = Color(0xFFF7CFE2)
    val Lilac = Color(0xFFF3E3FF)
    val Mint = Color(0xFFE8F7EF)
    val MintDeep = Color(0xFF2F6B4F)
    val Danger = Color(0xFF9B2C45)
    val DangerBg = Color(0xFFFFE8EE)
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
    MaterialTheme(
        colorScheme = Colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}

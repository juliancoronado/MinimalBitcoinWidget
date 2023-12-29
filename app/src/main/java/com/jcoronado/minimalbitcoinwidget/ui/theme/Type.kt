package com.jcoronado.minimalbitcoinwidget.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.jcoronado.minimalbitcoinwidget.R

val manropeFont = FontFamily(
    Font(R.font.manrope_bold, weight = FontWeight.Bold),
    Font(R.font.manrope_regular, weight = FontWeight.Normal),
)

// Set of Material typography styles to start with
val defaultTypography = Typography()

val Typography = Typography(

//    bodyLarge = TextStyle(
//        fontFamily = FontFamily.Default,
//        fontWeight = FontWeight.Normal,
//        fontSize = 16.sp,
//        lineHeight = 24.sp,
//        letterSpacing = 0.5.sp
//    ),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = manropeFont),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = manropeFont),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = manropeFont),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = manropeFont),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = manropeFont),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = manropeFont),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = manropeFont),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = manropeFont),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = manropeFont)
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)


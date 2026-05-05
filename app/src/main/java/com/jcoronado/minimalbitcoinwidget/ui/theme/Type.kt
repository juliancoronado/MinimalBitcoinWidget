package com.jcoronado.minimalbitcoinwidget.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.jcoronado.minimalbitcoinwidget.R

val googleSansCodeFontFamily = FontFamily(
    fonts = listOf(
        Font(
            resId = R.font.google_sans_code,
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val googleSansFlexFontFamily = FontFamily(
    fonts = listOf(
        Font(
            resId = R.font.google_sans_flex,
            weight = FontWeight.Normal,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(400)
            )
        ),
        Font(
            resId = R.font.google_sans_flex,
            weight = FontWeight.Medium,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(500)
            )
        ),
        Font(
            resId = R.font.google_sans_flex,
            weight = FontWeight.SemiBold,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(600)
            )
        ),
    )
)


val currentFont = googleSansFlexFontFamily

// default Material typography to start with
val defaultTypography = Typography()

val appTypography = Typography(
    // copy the current font we have selected to all the Typography styles but keep all other properties
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = currentFont),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = currentFont),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = currentFont),

    displaySmallEmphasized = defaultTypography.displaySmallEmphasized.copy(fontFamily = currentFont),
    displayMediumEmphasized = defaultTypography.displayMediumEmphasized.copy(fontFamily = currentFont),
    displayLargeEmphasized = defaultTypography.displayLargeEmphasized.copy(fontFamily = currentFont),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = currentFont),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = currentFont),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = currentFont),

    headlineSmallEmphasized = defaultTypography.headlineSmallEmphasized.copy(fontFamily = currentFont),
    headlineMediumEmphasized = defaultTypography.headlineMediumEmphasized.copy(fontFamily = currentFont),
    headlineLargeEmphasized = defaultTypography.headlineLargeEmphasized.copy(fontFamily = currentFont),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = currentFont),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = currentFont),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = currentFont),

    titleSmallEmphasized = defaultTypography.titleSmallEmphasized.copy(fontFamily = currentFont),
    titleMediumEmphasized = defaultTypography.titleMediumEmphasized.copy(fontFamily = currentFont),
    titleLargeEmphasized = defaultTypography.titleLargeEmphasized.copy(fontFamily = currentFont),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = currentFont),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = currentFont),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = currentFont),

    bodySmallEmphasized = defaultTypography.bodySmallEmphasized.copy(fontFamily = currentFont),
    bodyMediumEmphasized = defaultTypography.bodyMediumEmphasized.copy(fontFamily = currentFont),
    bodyLargeEmphasized = defaultTypography.bodyLargeEmphasized.copy(fontFamily = currentFont),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = currentFont),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = currentFont),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = currentFont),

    labelSmallEmphasized = defaultTypography.labelSmallEmphasized.copy(fontFamily = currentFont),
    labelMediumEmphasized = defaultTypography.labelMediumEmphasized.copy(fontFamily = currentFont),
    labelLargeEmphasized = defaultTypography.labelLargeEmphasized.copy(fontFamily = currentFont),
)
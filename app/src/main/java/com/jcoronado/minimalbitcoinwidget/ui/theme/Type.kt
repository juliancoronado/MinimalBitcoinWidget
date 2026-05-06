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
val appTypography = Typography(
    fontFamily = googleSansFlexFontFamily
)
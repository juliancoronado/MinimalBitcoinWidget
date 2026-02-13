package com.jcoronado.minimalbitcoinwidget.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.jcoronado.minimalbitcoinwidget.R

val gFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)
val googleSansCode = GoogleFont("Google Sans Code")
val googleSansFlex = GoogleFont("Google Sans Flex")
val googleSans = GoogleFont("Google Sans")

val googleSansCodeFontFamily = FontFamily(
    Font(googleFont = googleSansCode, fontProvider = gFontsProvider)
)

val googleSansFlexFontFamily = FontFamily(
    Font(googleFont = googleSansFlex, fontProvider = gFontsProvider)
)

val googleSansFontFamily = FontFamily(
    Font(googleFont = googleSans, fontProvider = gFontsProvider)
)

val currentFont = googleSansFontFamily

// default Material typography to start with
val defaultTypography = Typography()

val appTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = currentFont),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = currentFont),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = currentFont),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = currentFont),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = currentFont),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = currentFont),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = currentFont),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = currentFont),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = currentFont),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = currentFont),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = currentFont),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = currentFont),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = currentFont),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = currentFont),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = currentFont)
)
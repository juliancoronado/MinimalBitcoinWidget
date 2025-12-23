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
val fontName = GoogleFont("Google Sans Code")

val googleSansFontFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = gFontsProvider)
)

// Set of Material typography styles to start with
val defaultTypography = Typography()

val appTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = googleSansFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = googleSansFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = googleSansFontFamily),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = googleSansFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = googleSansFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = googleSansFontFamily),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = googleSansFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = googleSansFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = googleSansFontFamily),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = googleSansFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = googleSansFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = googleSansFontFamily),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = googleSansFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = googleSansFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = googleSansFontFamily)
)
package com.jcoronado.minimalbitcoinwidget.classes

import androidx.annotation.FontRes
import androidx.annotation.StringRes
import androidx.compose.ui.text.font.FontFamily
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.ui.theme.googleSansRoundedFontFamily

enum class WidgetFont(
    val key: String,
    @StringRes val labelResId: Int,
    @FontRes val fontResId: Int?,
    val letterSpacingEm: Float = 0f
) {
    APP_DEFAULT(
        key = "app_default",
        labelResId = R.string.font_app_default,
        fontResId = R.font.google_sans_flex_rounded,
        letterSpacingEm = 0.03f
    ),
    SYSTEM_DEFAULT(
        key = "system_default",
        labelResId = R.string.font_system_default,
        fontResId = null,
        letterSpacingEm = 0f
    );

    fun getFontFamily(): FontFamily {
        return if (fontResId != null) {
            googleSansRoundedFontFamily
        } else {
            FontFamily.SansSerif
        }
    }

    companion object {
        val DEFAULT = APP_DEFAULT

        fun fromKey(key: String?): WidgetFont {
            if (key.equals("google_sans_rounded", ignoreCase = true) || key.equals("google_sans_code", ignoreCase = true)) {
                return APP_DEFAULT
            }
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: DEFAULT
        }
    }
}

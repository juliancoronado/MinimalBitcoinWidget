package com.jcoronado.minimalbitcoinwidget.classes

import androidx.annotation.FontRes
import androidx.annotation.StringRes
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.jcoronado.minimalbitcoinwidget.R

enum class WidgetFont(
    val key: String,
    @StringRes val labelResId: Int,
    @FontRes val fontResId: Int?
) {
    GOOGLE_SANS_ROUNDED(
        key = "google_sans_rounded",
        labelResId = R.string.font_google_sans_rounded,
        fontResId = R.font.google_sans_flex_rounded
    ),
    GOOGLE_SANS_CODE(
        key = "google_sans_code",
        labelResId = R.string.font_google_sans_code,
        fontResId = R.font.google_sans_code
    ),
    SYSTEM_DEFAULT(
        key = "system_default",
        labelResId = R.string.font_system_default,
        fontResId = null
    );

    fun getFontFamily(): FontFamily {
        return if (fontResId != null) {
            FontFamily(Font(fontResId))
        } else {
            FontFamily.Default
        }
    }

    companion object {
        val DEFAULT = GOOGLE_SANS_ROUNDED

        fun fromKey(key: String?): WidgetFont {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: DEFAULT
        }
    }
}

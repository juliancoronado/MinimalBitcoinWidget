package com.jcoronado.minimalbitcoinwidget.classes

import androidx.annotation.StringRes
import com.jcoronado.minimalbitcoinwidget.R

enum class WidgetTheme(
    val key: String,
    @StringRes val labelResId: Int
) {
    SYSTEM("system", R.string.widget_theme_system),
    LIGHT("light", R.string.widget_theme_light),
    DARK("dark", R.string.widget_theme_dark);

    companion object {
        fun fromKey(key: String?): WidgetTheme {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: SYSTEM
        }
    }
}

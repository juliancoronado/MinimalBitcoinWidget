package com.jcoronado.minimalbitcoinwidget.classes

import androidx.annotation.StringRes
import com.jcoronado.minimalbitcoinwidget.R

enum class WidgetColorStyle(
    val key: String,
    @StringRes val labelResId: Int
) {
    DYNAMIC("dynamic", R.string.widget_colors_dynamic),
    SOLID("solid", R.string.widget_colors_solid);

    companion object {
        fun fromKey(key: String?): WidgetColorStyle {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: DYNAMIC
        }
    }
}

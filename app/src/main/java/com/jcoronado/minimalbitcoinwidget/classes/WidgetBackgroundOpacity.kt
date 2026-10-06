package com.jcoronado.minimalbitcoinwidget.classes

import androidx.annotation.StringRes
import com.jcoronado.minimalbitcoinwidget.R

enum class WidgetBackgroundOpacity(
    val key: String,
    @StringRes val labelResId: Int,
    val alpha: Float
) {
    SOLID("solid", R.string.widget_opacity_solid, 1.0f),
    SEMI_TRANSPARENT("semi_transparent", R.string.widget_opacity_semi_transparent, 0.5f),
    TRANSPARENT("transparent", R.string.widget_opacity_transparent, 0.0f);

    companion object {
        fun fromKey(key: String?): WidgetBackgroundOpacity {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: SOLID
        }
    }
}

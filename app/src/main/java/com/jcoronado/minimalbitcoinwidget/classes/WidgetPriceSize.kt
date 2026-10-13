package com.jcoronado.minimalbitcoinwidget.classes

import androidx.annotation.StringRes
import com.jcoronado.minimalbitcoinwidget.R

enum class WidgetPriceSize(
    val key: String,
    @StringRes val labelResId: Int,
    val scaleFactor: Float
) {
    SMALL("small", R.string.widget_size_small, 0.85f),
    DEFAULT("default", R.string.widget_size_default, 1.0f),
    LARGE("large", R.string.widget_size_large, 1.15f);

    companion object {
        fun fromKey(key: String?): WidgetPriceSize {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: DEFAULT
        }
    }
}

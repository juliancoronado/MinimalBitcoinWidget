package com.jcoronado.minimalbitcoinwidget.utils

import com.jcoronado.minimalbitcoinwidget.R

enum class TimeInterval(val value: Int, val labelResId: Int) {
    HOURS_24(value = 0, labelResId = R.string.interval_24h),
    DAYS_7(value = 1, labelResId = R.string.interval_7d),
    DAYS_30(value = 2, labelResId = R.string.interval_30d);

    companion object {
        // default to 24H if no matching value is found
        fun fromValue(value: Int): TimeInterval {
            return entries.find { it.value == value } ?: HOURS_24
        }
    }
}
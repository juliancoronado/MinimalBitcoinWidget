package com.jcoronado.minimalbitcoinwidget

import java.util.concurrent.TimeUnit

/**
 * An object to hold all SharedPreferences keys used throughout the app.
 */
object Prefs {
    const val LAST_API_CALL_TIMESTAMP = "app_last_api_call_timestamp"
    const val CACHED_PRICE_DATA = "app_cached_price_data"
    const val SELECTED_CURRENCY = "currency"
    const val CURRENCY_DEFAULT = "usd"
}

object AppConstants {
    private const val CACHE_DURATION_MINUTES = 29L
    val CACHE_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(CACHE_DURATION_MINUTES)
}
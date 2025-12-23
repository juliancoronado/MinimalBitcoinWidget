package com.jcoronado.minimalbitcoinwidget.classes

import java.util.concurrent.TimeUnit

/**
 * Holds the SharedPreferences keys.
 */
object Prefs {
    const val LAST_API_CALL_TIMESTAMP = "app_last_api_call_timestamp"
    const val CACHED_PRICE_DATA = "app_cached_price_data_v1"
    const val SELECTED_CURRENCY = "currency"
}

/**
 * Holds app constant values.
 */
object AppConstants {
    const val CURRENCY_DEFAULT = "usd"
    private const val CACHE_DURATION_MINUTES = 30L - 1L
    val CACHE_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(CACHE_DURATION_MINUTES)
}

/**
 * Holds API URL constant values.
 */
object Api {
    private const val COINGECKO_BASE_URL = "https://api.coingecko.com/api/v3"
    private const val COINGECKO_ENDPOINT = "/simple/price"
    private const val QUERY_PARAMS = "?ids=bitcoin&include_24hr_change=true&precision=2&vs_currencies="
    const val COINGECKO_API_URL = COINGECKO_BASE_URL + COINGECKO_ENDPOINT + QUERY_PARAMS
}
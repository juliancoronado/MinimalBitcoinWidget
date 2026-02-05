package com.jcoronado.minimalbitcoinwidget.classes

import java.util.concurrent.TimeUnit

/**
 * Contains SharedPreferences keys used throughout the application.
 */
object Prefs {
    /** Key for storing the timestamp of the last successful API call. */
    const val LAST_API_CALL_TIMESTAMP = "app_last_api_call_timestamp"
    /** Key for storing the cached Bitcoin price data. */
    const val CACHED_PRICE_DATA = "app_cached_price_data_v1"
    /** Key for storing the user's selected currency preference. */
    const val SELECTED_CURRENCY = "currency"
    const val DYNAMIC_COLORS = "dynamic_colors"
    const val SELECTED_THEME = "selected_theme"
    const val REFRESH_INTERVAL = "refresh_interval"
}

/**
 * Contains general application constants.
 */
object AppConstants {
    /** Default currency used if none is selected. */
    const val CURRENCY_DEFAULT = "usd"
    /** Internal cache duration in minutes. */
    private const val CACHE_DURATION_MINUTES = 30L
    /** Duration in milliseconds for which the cached price data is considered valid. */
    val CACHE_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(CACHE_DURATION_MINUTES)
    const val DEBUG_MODE = false
}

/**
 * Contains API related constants and URL construction.
 */
object Api {
    /** Base URL for the CoinGecko API. */
    private const val COINGECKO_BASE_URL = "https://api.coingecko.com/api/v3"
    /** Endpoint for fetching simple price data. */
    private const val COINGECKO_ENDPOINT = "/simple/price"
    /** Query parameters for the Bitcoin price request, including 24h change and precision. */
    private const val QUERY_PARAMS = "?ids=bitcoin&include_24hr_change=true&precision=2&vs_currencies="
    /** Complete URL for fetching Bitcoin price data from CoinGecko. */
    const val COINGECKO_API_URL = COINGECKO_BASE_URL + COINGECKO_ENDPOINT + QUERY_PARAMS
}

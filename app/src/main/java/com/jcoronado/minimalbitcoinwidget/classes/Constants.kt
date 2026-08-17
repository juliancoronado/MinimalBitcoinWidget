package com.jcoronado.minimalbitcoinwidget.classes

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.jcoronado.minimalbitcoinwidget.BuildConfig
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
    const val SELECTED_CHANGE_PERCENTAGE = "selected_change_percentage"
    const val SHOW_SPARKLINE = "show_sparkline"
    const val DEVELOPER_MODE_ENABLED = "developer_mode_enabled"
    /** Key for storing the app version code to detect updates. */
    const val LAST_VERSION_CODE = "last_version_code"

    const val DEBUG_MOCK_UI_ENABLED = "debug_mock_ui_enabled"
    const val DEBUG_MOCK_PRICE = "debug_mock_price"
    const val DEBUG_MOCK_PERCENT_CHANGE = "debug_mock_percent_change"
    const val DEBUG_MOCK_CURRENCY = "debug_mock_currency"

    /**
     * Checks if the app was updated from a pre-v3.0.0 release (versionCode < 12 or legacy prefs exist
     * without a recorded version code) and invalidates the cache if so.
     * For modern v3.x updates, it updates the saved version code without wiping the cache.
     */
    fun checkAppUpdateAndInvalidateCache(prefs: SharedPreferences) {
        val currentVersionCode = BuildConfig.VERSION_CODE
        val lastVersionCode = prefs.getInt(LAST_VERSION_CODE, 0)

        // Pre-v3.0.0 builds didn't store LAST_VERSION_CODE. If lastVersionCode is 0 but legacy
        // preferences exist, or if lastVersionCode is 1..11, it's a legacy pre-v3.0.0 upgrade.
        val isLegacyUpgrade = (lastVersionCode in 1..11) || 
                (lastVersionCode == 0 && prefs.contains(LAST_API_CALL_TIMESTAMP))

        if (isLegacyUpgrade) {
            Log.i("Prefs", "Upgrading from pre-v3.0.0 ($lastVersionCode -> $currentVersionCode). Invalidating price cache.")
            prefs.edit(commit = true) {
                putLong(LAST_API_CALL_TIMESTAMP, 0L)
                remove(CACHED_PRICE_DATA)
                putInt(LAST_VERSION_CODE, currentVersionCode)
            }
        } else if (currentVersionCode > lastVersionCode) {
            // Keep tracked version code updated without wiping cache for modern v3.x updates
            prefs.edit(commit = true) {
                putInt(LAST_VERSION_CODE, currentVersionCode)
            }
        }
    }
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
    const val WIDGET_DEBUG_MODE = false

    const val DEBUG_MOCK_PRICE_DEFAULT = "52849.10"
    const val DEBUG_MOCK_PERCENT_CHANGE_DEFAULT = "2.03"
    const val DEBUG_MOCK_CURRENCY_DEFAULT = "GBP"
}

/**
 * Contains API related constants and URL construction.
 */
object Api {
    /** Base URL for the CoinGecko API. */
    private const val COINGECKO_BASE_URL = "https://api.coingecko.com/api/v3/"
    /** Endpoint for fetching coin market price data. */
    private const val COINGECKO_ENDPOINT = "coins/markets"
    /** Query parameters for the Bitcoin price request, including 24h change, sparkline, and precision. */
    private const val QUERY_PARAMS = "?ids=bitcoin&precision=2&price_change_percentage=24h,7d,30d&sparkline=true&vs_currency="
    /** Complete URL for fetching Bitcoin price data from CoinGecko. */
    const val COINGECKO_API_URL = COINGECKO_BASE_URL + COINGECKO_ENDPOINT + QUERY_PARAMS
}

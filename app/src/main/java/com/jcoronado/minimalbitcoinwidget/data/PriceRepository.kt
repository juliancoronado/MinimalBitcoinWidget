package com.jcoronado.minimalbitcoinwidget.data

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jcoronado.minimalbitcoinwidget.classes.Api
import com.jcoronado.minimalbitcoinwidget.classes.AppConstants
import com.jcoronado.minimalbitcoinwidget.classes.Prefs
import com.jcoronado.minimalbitcoinwidget.classes.PriceData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val TAG = "PriceRepository"

/**
 * Repository responsible for managing Bitcoin price data fetching, local caching,
 * and debug mock data configuration.
 */
class PriceRepository(private val context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Returns true if Mock UI mode is enabled in Developer Options.
     */
    fun isMockUiEnabled(): Boolean {
        return prefs.getBoolean(Prefs.DEBUG_MOCK_UI_ENABLED, false)
    }

    /**
     * Constructs a [PriceData] object using mock configuration values from SharedPreferences.
     */
    fun getMockPriceData(): PriceData {
        val mockPrice = prefs.getString(Prefs.DEBUG_MOCK_PRICE, AppConstants.DEBUG_MOCK_PRICE_DEFAULT)
            ?.toDoubleOrNull() ?: AppConstants.DEBUG_MOCK_PRICE_DEFAULT.toDouble()
        val mockPercent = prefs.getString(Prefs.DEBUG_MOCK_PERCENT_CHANGE, AppConstants.DEBUG_MOCK_PERCENT_CHANGE_DEFAULT)
            ?.toDoubleOrNull() ?: AppConstants.DEBUG_MOCK_PERCENT_CHANGE_DEFAULT.toDouble()

        // Generate 168 synthetic hourly sparkline points trending from start price to current mock price
        val startPrice = mockPrice / (1.0 + (mockPercent / 100.0))
        val mockSparklinePrices = List(168) { i ->
            val progress = i / 167.0
            val baseLinear = startPrice + (mockPrice - startPrice) * progress
            // Add subtle sine wave noise for visual curve variation
            val wave = kotlin.math.sin(i * 0.15) * (mockPrice * 0.008)
            baseLinear + wave
        }

        return PriceData(
            currentPrice = mockPrice,
            priceChangePercentage24h = mockPercent,
            priceChangePercentage7d = mockPercent,
            priceChangePercentage30d = mockPercent,
            sparklineIn7d = PriceData.SparklineData(prices = mockSparklinePrices)
        )
    }

    /**
     * Gets the current mock currency preference.
     */
    fun getMockCurrency(): String {
        return prefs.getString(Prefs.DEBUG_MOCK_CURRENCY, AppConstants.DEBUG_MOCK_CURRENCY_DEFAULT)
            ?: AppConstants.DEBUG_MOCK_CURRENCY_DEFAULT
    }

    /**
     * Deserializes and returns cached [PriceData] from SharedPreferences, or null if unparseable/missing.
     */
    fun getCachedPriceData(): PriceData? {
        val cachedDataJson = prefs.getString(Prefs.CACHED_PRICE_DATA, null) ?: return null
        return try {
            gson.fromJson(cachedDataJson, PriceData::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse cached price data JSON: ${e.message}")
            null
        }
    }

    /**
     * Gets the last API call timestamp in milliseconds.
     */
    fun getLastApiCallTimestamp(): Long {
        return prefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, 0L)
    }

    /**
     * Gets the selected currency from SharedPreferences.
     */
    fun getSelectedCurrency(): String {
        return prefs.getString(Prefs.SELECTED_CURRENCY, AppConstants.CURRENCY_DEFAULT)
            ?: AppConstants.CURRENCY_DEFAULT
    }

    /**
     * Updates and persists the selected currency.
     */
    fun updateSelectedCurrency(newCurrency: String) {
        prefs.edit {
            putString(Prefs.SELECTED_CURRENCY, newCurrency)
        }
    }

    /**
     * Checks whether existing cached data is fresh (fetched within [AppConstants.CACHE_DURATION_MILLIS]).
     */
    fun isCacheFresh(): Boolean {
        val lastApiCallTime = getLastApiCallTimestamp()
        val currentTime = System.currentTimeMillis()
        val hasCache = prefs.contains(Prefs.CACHED_PRICE_DATA)
        return hasCache && (currentTime - lastApiCallTime < AppConstants.CACHE_DURATION_MILLIS)
    }

    /**
     * Saves updated [PriceData] and the current timestamp to local cache.
     */
    fun savePriceDataToCache(priceData: PriceData, timestamp: Long = System.currentTimeMillis()) {
        prefs.edit {
            putLong(Prefs.LAST_API_CALL_TIMESTAMP, timestamp)
            putString(Prefs.CACHED_PRICE_DATA, gson.toJson(priceData))
        }
    }

    /**
     * Checks app version updates and invalidates cache if necessary.
     */
    fun checkAndInvalidateCache() {
        Prefs.checkAppUpdateAndInvalidateCache(prefs)
    }

    /**
     * Fetches price data for the specified currency.
     *
     * - If [isMockUiEnabled] is true, returns mock price data.
     * - If [force] is false and cache is fresh, returns cached price data without making a network call.
     * - Otherwise, executes an HTTP request to CoinGecko API and updates the local cache.
     */
    suspend fun fetchPrice(currency: String, force: Boolean = false): Resource<PriceData> = withContext(Dispatchers.IO) {
        checkAndInvalidateCache()

        if (isMockUiEnabled()) {
            return@withContext Resource.Success(getMockPriceData())
        }

        if (!force && isCacheFresh()) {
            val cachedData = getCachedPriceData()
            if (cachedData != null) {
                return@withContext Resource.Success(cachedData)
            }
        }

        val url = Api.COINGECKO_API_URL + currency
        val request = Request.Builder().url(url).build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body.string()
                val type = object : TypeToken<List<PriceData>>() {}.type
                val dataList: List<PriceData> = gson.fromJson(body, type)

                if (dataList.isEmpty()) {
                    return@withContext Resource.Error("Empty API response")
                }

                val priceData = dataList[0]
                savePriceDataToCache(priceData)
                Resource.Success(priceData)
            } else {
                Log.w(TAG, "Unsuccessful GET request: ${response.code}")
                Resource.Error("Server Error: ${response.code}")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network call failed", e)
            Resource.Error(e.message ?: "Network call failed", cause = e)
        } catch (e: Exception) {
            Log.e(TAG, "API processing failed", e)
            Resource.Error(e.message ?: "API processing failed", cause = e)
        }
    }
}

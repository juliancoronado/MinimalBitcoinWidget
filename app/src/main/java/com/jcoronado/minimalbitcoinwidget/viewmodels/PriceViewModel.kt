package com.jcoronado.minimalbitcoinwidget.viewmodels

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.edit
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.Api
import com.jcoronado.minimalbitcoinwidget.classes.AppConstants
import com.jcoronado.minimalbitcoinwidget.classes.Prefs
import com.jcoronado.minimalbitcoinwidget.classes.PriceData
import com.jcoronado.minimalbitcoinwidget.classes.PriceUiState
import com.jcoronado.minimalbitcoinwidget.utils.TimeInterval
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidget
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetState
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetStateDefinition
import com.jcoronado.minimalbitcoinwidget.widgets.legacy.getCurrencyInfo
import com.jcoronado.minimalbitcoinwidget.widgets.legacy.setWidgetViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

private const val LOG_TAG = "PriceViewModel"

class PriceViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PriceUiState(isLoading = true))

    // read-only state
    val uiState: StateFlow<PriceUiState> = _uiState.asStateFlow()
    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)
    private val client = OkHttpClient.Builder().build()
    private val gson = Gson()

    init {
        Prefs.checkAppUpdateAndInvalidateCache(prefs)
        loadInitialData()
        fetchPrice(fromInit = true)
    }

    /** Load initial data from SharedPreferences. */
    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            val cachedDataJson = prefs.getString(Prefs.CACHED_PRICE_DATA, null)
            val cachedCurrency =
                prefs.getString(Prefs.SELECTED_CURRENCY, AppConstants.CURRENCY_DEFAULT)!!
            if (cachedDataJson != null) {
                try {
                    val lastUpdated = prefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, 0L)
                    val cachedData = gson.fromJson(cachedDataJson, PriceData::class.java)

                    val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, 0)
                    val percentage = when (selectedInterval) {
                        0 -> cachedData.priceChangePercentage24h
                        1 -> cachedData.priceChangePercentage7d
                        2 -> cachedData.priceChangePercentage30d
                        else -> cachedData.priceChangePercentage24h
                    }

                    val interval = TimeInterval.fromValue(selectedInterval)

                    // update state immediately
                    _uiState.value = _uiState.value.copy(
                        price = cachedData.currentPrice,
                        percentageChange = percentage,
                        changeIntervalLabelResId = interval.labelResId,
                        selectedCurrency = cachedCurrency,
                        lastUpdated = lastUpdated
                    )
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Failed to parse initial cache $e")
                }
            }
        }
    }

    fun refreshFromCache() {
        loadInitialData()
        redrawWidgets()
    }

    /** Update the selected currency and persist it. */
    fun updateCurrency(newCurrency: String) {
        if (_uiState.value.selectedCurrency == newCurrency) return

        Log.i(LOG_TAG, "Updating currency to: $newCurrency")

        _uiState.value = _uiState.value.copy(selectedCurrency = newCurrency)
        prefs.edit {
            putString(Prefs.SELECTED_CURRENCY, newCurrency)
        }
        fetchPrice(force = true)
    }

    /** Fetch data from the Coingecko API. */
    fun fetchPrice(force: Boolean = false, fromInit: Boolean = false) {
        if (!fromInit && _uiState.value.isLoading) {
            Log.d(LOG_TAG, "Already loading, skipping this fetch")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val lastApiCallTime = prefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, 0L)
            val currentTime = System.currentTimeMillis()

            val hasCache = prefs.contains(Prefs.CACHED_PRICE_DATA)
            val isFresh = (currentTime - lastApiCallTime < AppConstants.CACHE_DURATION_MILLIS)

            if (!force && hasCache && isFresh) {
                Log.d(LOG_TAG, "Using cached data")
                delay(750)

                val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, 0)
                val interval = TimeInterval.fromValue(selectedInterval)

                _uiState.value = _uiState.value.copy(
                    lastUpdated = lastApiCallTime,
                    changeIntervalLabelResId = interval.labelResId,
                    isLoading = false
                )
                redrawWidgets()
                return@launch
            }

            val currency = _uiState.value.selectedCurrency
            val url = Api.COINGECKO_API_URL + currency
            val request = Request.Builder().url(url).build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Log.d(LOG_TAG, "Successful GET request: ${response.code}")
                    val body = response.body.string()

                    val type = object : TypeToken<List<PriceData>>() {}.type
                    val dataList: List<PriceData> = gson.fromJson(body, type)

                    Log.d("PriceViewModel", "Body: $body")

                    if (dataList.isEmpty()) throw Exception("Empty response")

                    val priceData = dataList[0]
                    val lastUpdated = System.currentTimeMillis()

                    // update cached values
                    prefs.edit {
                        putLong(Prefs.LAST_API_CALL_TIMESTAMP, lastUpdated)
                        putString(Prefs.CACHED_PRICE_DATA, gson.toJson(priceData))
                    }

                    val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, 0)
                    val percentage = when (selectedInterval) {
                        0 -> priceData.priceChangePercentage24h
                        1 -> priceData.priceChangePercentage7d
                        2 -> priceData.priceChangePercentage30d
                        else -> priceData.priceChangePercentage24h
                    }

                    val interval = TimeInterval.fromValue(selectedInterval)

                    delay(750)

                    _uiState.value = _uiState.value.copy(
                        price = priceData.currentPrice,
                        percentageChange = percentage,
                        changeIntervalLabelResId = interval.labelResId,
                        isLoading = false,
                        errorMessage = null,
                        lastUpdated = lastUpdated
                    )

                    // trigger widget update to sync with new data
                    redrawWidgets()
                } else {
                    Log.w(LOG_TAG, "Unsuccessful GET request: ${response.code}")
                    delay(1000)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, errorMessage = "Server Error: ${response.code}"
                    )
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Network call failed: ${e.message}")
                delay(1000)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Error: $e")
            }
        }
    }

    /**
     * Triggers a manual update for all placed instances of both legacy and Glance widgets.
     */
    fun redrawWidgets() {
        refreshWidgetsFromCache(getApplication())
    }

    companion object {
        const val LEGACY_WIDGET_WRAPPER_CLASS = "com.jcoronado.minimalbitcoinwidget.PriceWidget"

        /**
         * Re-reads the cached price data and updates all widget instances (Glance and Legacy).
         * This is useful for UI-only updates when settings like currency or interval change.
         */
        fun refreshWidgetsFromCache(context: Context) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val cachedDataJson = prefs.getString(Prefs.CACHED_PRICE_DATA, null) ?: return
            val gson = Gson()

            try {
                val priceData = gson.fromJson(cachedDataJson, PriceData::class.java)
                val currencyCode =
                    prefs.getString(Prefs.SELECTED_CURRENCY, AppConstants.CURRENCY_DEFAULT)
                        ?: AppConstants.CURRENCY_DEFAULT

                // define glance widget state
                val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, 0)
                val percentage = when (selectedInterval) {
                    0 -> priceData.priceChangePercentage24h
                    1 -> priceData.priceChangePercentage7d
                    2 -> priceData.priceChangePercentage30d
                    else -> priceData.priceChangePercentage24h
                }

                val interval = TimeInterval.fromValue(selectedInterval)

                val glanceState = PriceWidgetState.Available(
                    price = priceData.currentPrice,
                    changePercentage = percentage,
                    intervalLabelResId = interval.labelResId,
                    currency = currencyCode
                )

                // update glance widgets
                CoroutineScope(Dispatchers.IO).launch {
                    updateGlanceWidgets(context, glanceState)
                }

                // update legacy widgets
                updateLegacyWidgets(context, priceData)

            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to refresh widgets from cache: $e")
            }
        }

        /**
         * Updates all Glance widgets with the given state.
         */
        suspend fun updateGlanceWidgets(context: Context, state: PriceWidgetState) {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(PriceWidget::class.java)
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, PriceWidgetStateDefinition, glanceId) {
                    state
                }
            }
            PriceWidget().updateAll(context)
        }

        /**
         * Updates all legacy widgets with the given price data.
         */
        fun updateLegacyWidgets(context: Context, priceData: PriceData) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, LEGACY_WIDGET_WRAPPER_CLASS)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isEmpty()) return

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val currencyCode =
                prefs.getString(Prefs.SELECTED_CURRENCY, AppConstants.CURRENCY_DEFAULT)
                    ?: AppConstants.CURRENCY_DEFAULT
            val currencyInfo = getCurrencyInfo(currencyCode)

            val views = RemoteViews(context.packageName, R.layout.legacy_price_widget)
            setWidgetViews(context, views, priceData, currencyInfo, loading = false)

            appWidgetIds.forEach { appWidgetId ->
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}

package com.jcoronado.minimalbitcoinwidget.viewmodels

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import androidx.core.content.edit
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jcoronado.minimalbitcoinwidget.classes.Api
import com.jcoronado.minimalbitcoinwidget.classes.AppConstants
import com.jcoronado.minimalbitcoinwidget.classes.Prefs
import com.jcoronado.minimalbitcoinwidget.classes.PriceData
import com.jcoronado.minimalbitcoinwidget.classes.PriceUiState
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidget
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetState
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetStateDefinition
import com.jcoronado.minimalbitcoinwidget.widgets.legacy.LegacyPriceWidget
import com.jcoronado.minimalbitcoinwidget.widgets.legacy.getCurrencyInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val LOG_TAG = "PriceViewModel"

class PriceViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PriceUiState(isLoading = true))

    // read-only state
    val uiState: StateFlow<PriceUiState> = _uiState.asStateFlow()
    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)
    private val client = OkHttpClient.Builder().build()
    private val gson = Gson()

    init {
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
                    val intervalLabel = when (selectedInterval) {
                        0 -> "24H"
                        1 -> "7D"
                        2 -> "30D"
                        else -> "24H"
                    }

                    // update state immediately
                    _uiState.value = _uiState.value.copy(
                        price = cachedData.currentPrice,
                        percentageChange = percentage,
                        changeIntervalLabel = intervalLabel,
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
                val intervalLabel = when (selectedInterval) {
                    0 -> "24H"
                    1 -> "7D"
                    2 -> "30D"
                    else -> "24H"
                }

                _uiState.value = _uiState.value.copy(
                    lastUpdated = lastApiCallTime,
                    changeIntervalLabel = intervalLabel,
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
                    val intervalLabel = when (selectedInterval) {
                        0 -> "24H"
                        1 -> "7D"
                        2 -> "30D"
                        else -> "24H"
                    }

                    delay(750)

                    _uiState.value = _uiState.value.copy(
                        price = priceData.currentPrice,
                        percentageChange = percentage,
                        changeIntervalLabel = intervalLabel,
                        isLoading = false,
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
    private fun redrawWidgets() {
        val context = getApplication<Application>()

        // update glance widgets using PriceWidgetStateDefinition
        viewModelScope.launch {
            try {
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(PriceWidget::class.java)

                Log.d(LOG_TAG, "Updating ${glanceIds.size} widgets: $glanceIds")

                val currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
                
                val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, 0)
                val intervalLabel = when (selectedInterval) {
                    0 -> "24H"
                    1 -> "7D"
                    2 -> "30D"
                    else -> "24H"
                }

                glanceIds.forEach { glanceId ->
                    updateAppWidgetState(context, PriceWidgetStateDefinition, glanceId) {
                        PriceWidgetState.Available(
                            price = _uiState.value.price,
                            changePercentage = _uiState.value.percentageChange,
                            intervalLabel = intervalLabel,
                            currency = _uiState.value.selectedCurrency,
                            symbol = getCurrencyInfo(_uiState.value.selectedCurrency).symbol,
                            lastUpdated = currentTime,
                            debug = AppConstants.DEBUG_MODE
                        )
                    }
                }
                PriceWidget().updateAll(context)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to update Glance widgets: $e")
            }
        }

        // Update Legacy Widgets (PriceWidget)
        val intent = Intent(context, LegacyPriceWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, LegacyPriceWidget::class.java)
            )
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }
}

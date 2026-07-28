package com.jcoronado.minimalbitcoinwidget.viewmodels

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.AppConstants
import com.jcoronado.minimalbitcoinwidget.classes.Prefs
import com.jcoronado.minimalbitcoinwidget.classes.PriceData
import com.jcoronado.minimalbitcoinwidget.classes.PriceUiState
import com.jcoronado.minimalbitcoinwidget.data.PriceRepository
import com.jcoronado.minimalbitcoinwidget.data.Resource
import com.jcoronado.minimalbitcoinwidget.utils.TimeInterval
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidget
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetReceiver
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

private const val LOG_TAG = "PriceViewModel"

class PriceViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: PriceRepository = PriceRepository(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PriceUiState(isLoading = true))

    // read-only state
    val uiState: StateFlow<PriceUiState> = _uiState.asStateFlow()
    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

    init {
        // First check if the app version updated to invalidate any outdated cache.
        repository.checkAndInvalidateCache()
        
        // Load settings and cached price synchronously to ensure the selected currency
        // is fully populated in _uiState before starting any network calls.
        loadInitialData()
        
        // Trigger a fresh price fetch using the newly loaded currency preference.
        fetchPrice(fromInit = true)
    }

    /** Load initial data from Repository/SharedPreferences. */
    private fun loadInitialData() {
        if (repository.isMockUiEnabled()) {
            val mockData = repository.getMockPriceData()
            val mockCurrency = repository.getMockCurrency()

            _uiState.value = PriceUiState(
                price = mockData.currentPrice,
                percentageChange = mockData.priceChangePercentage24h,
                isLoading = false,
                selectedCurrency = mockCurrency,
                changeIntervalLabelResId = R.string.interval_24h,
                lastUpdated = 0L
            )
            return
        }

        val cachedCurrency = repository.getSelectedCurrency()
        val cachedData = repository.getCachedPriceData()
        if (cachedData != null) {
            try {
                val lastUpdated = repository.getLastApiCallTimestamp()
                val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, 0)
                val percentage = cachedData.getPercentageForInterval(selectedInterval)

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
                _uiState.value = _uiState.value.copy(selectedCurrency = cachedCurrency)
            }
        } else {
            _uiState.value = _uiState.value.copy(selectedCurrency = cachedCurrency)
        }
    }

    fun refreshFromCache() {
        loadInitialData()
        redrawWidgets()
    }

    /**
     * Requests the system to pin the modern Glance widget to the homescreen.
     */
    fun requestPinWidget() {
        viewModelScope.launch {
            Log.d(LOG_TAG, "Requesting to pin Glance widget")
            var currentState = uiState.value

            // update currentState to use default values (if needed)
            // so the widget previews look complete
            currentState = PriceUiState(
                isLoading = false,
                changeIntervalLabelResId = currentState.changeIntervalLabelResId,
                percentageChange = if (currentState.percentageChange == 0.0) 2.03 else currentState.percentageChange,
                price = if (currentState.price == 0.0) 52849.10 else currentState.price,
                errorMessage = null
            )

            try {
                GlanceAppWidgetManager(getApplication()).requestPinGlanceAppWidget(
                    receiver = PriceWidgetReceiver::class.java,
                    preview = PriceWidget(),
                    previewState = PriceWidgetState.Available(
                        price = currentState.price,
                        changePercentage = currentState.percentageChange,
                        intervalLabelResId = currentState.changeIntervalLabelResId,
                        currency = currentState.selectedCurrency
                    )
                )
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to request pin widget: $e")
            }
        }
    }

    /** Update the selected currency and persist it via Repository. */
    fun updateCurrency(newCurrency: String) {
        if (_uiState.value.selectedCurrency == newCurrency) return

        Log.i(LOG_TAG, "Updating currency to: $newCurrency")

        _uiState.value = _uiState.value.copy(selectedCurrency = newCurrency)
        repository.updateSelectedCurrency(newCurrency)
        fetchPrice(force = true)
    }

    /** Fetch data via Repository. */
    fun fetchPrice(force: Boolean = false, fromInit: Boolean = false) {
        if (repository.isMockUiEnabled()) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                delay(750)
                loadInitialData()
                redrawWidgets()
            }
            return
        }

        if (!fromInit && _uiState.value.isLoading) {
            Log.d(LOG_TAG, "Already loading, skipping this fetch")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val currency = _uiState.value.selectedCurrency
            val resource = repository.fetchPrice(currency, force = force)

            delay(750)

            when (resource) {
                is Resource.Success -> {
                    val priceData = resource.data
                    val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, 0)
                    val percentage = priceData.getPercentageForInterval(selectedInterval)
                    val interval = TimeInterval.fromValue(selectedInterval)
                    val lastUpdated = repository.getLastApiCallTimestamp()

                    _uiState.value = _uiState.value.copy(
                        price = priceData.currentPrice,
                        percentageChange = percentage,
                        changeIntervalLabelResId = interval.labelResId,
                        isLoading = false,
                        errorMessage = null,
                        lastUpdated = lastUpdated
                    )
                    redrawWidgets()
                }
                is Resource.Error -> {
                    Log.e(LOG_TAG, "Network call failed: ${resource.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = resource.message
                    )
                }
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
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
            val repository = PriceRepository(context)

            if (repository.isMockUiEnabled()) {
                val mockData = repository.getMockPriceData()
                val mockCurrency = repository.getMockCurrency()

                val glanceState = PriceWidgetState.Available(
                    price = mockData.currentPrice,
                    changePercentage = mockData.priceChangePercentage24h,
                    intervalLabelResId = R.string.interval_24h,
                    currency = mockCurrency
                )

                // update glance widgets
                CoroutineScope(Dispatchers.IO).launch {
                    updateGlanceWidgets(context, glanceState)
                }

                // update legacy widgets
                updateLegacyWidgets(context, mockData, mockCurrency)
                return
            }

            val priceData = repository.getCachedPriceData() ?: return
            val currencyCode = repository.getSelectedCurrency()

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, 0)
            val percentage = priceData.getPercentageForInterval(selectedInterval)
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
        fun updateLegacyWidgets(context: Context, priceData: PriceData, currencyOverride: String? = null) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, LEGACY_WIDGET_WRAPPER_CLASS)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isEmpty()) return

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val currencyCode = currencyOverride ?: (prefs.getString(Prefs.SELECTED_CURRENCY, AppConstants.CURRENCY_DEFAULT)
                    ?: AppConstants.CURRENCY_DEFAULT)
            val currencyInfo = getCurrencyInfo(currencyCode)

            val views = RemoteViews(context.packageName, R.layout.legacy_price_widget)
            setWidgetViews(context, views, priceData, currencyInfo, loading = false)

            appWidgetIds.forEach { appWidgetId ->
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}


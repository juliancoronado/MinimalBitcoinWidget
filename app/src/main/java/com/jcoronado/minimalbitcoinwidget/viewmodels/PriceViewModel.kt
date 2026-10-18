package com.jcoronado.minimalbitcoinwidget.viewmodels

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
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
import com.jcoronado.minimalbitcoinwidget.classes.WidgetFont
import com.jcoronado.minimalbitcoinwidget.data.PriceRepository
import com.jcoronado.minimalbitcoinwidget.data.Resource
import com.jcoronado.minimalbitcoinwidget.utils.FormatUtils
import com.jcoronado.minimalbitcoinwidget.utils.TimeInterval
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidget
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetReceiver
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetState
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetStateDefinition
import com.jcoronado.minimalbitcoinwidget.widgets.glance.ChartPriceWidget
import com.jcoronado.minimalbitcoinwidget.widgets.glance.ChartPriceWidgetReceiver
import com.jcoronado.minimalbitcoinwidget.widgets.legacy.getCurrencyInfo
import com.jcoronado.minimalbitcoinwidget.widgets.legacy.setWidgetViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.jcoronado.minimalbitcoinwidget.AppDatabase
import com.jcoronado.minimalbitcoinwidget.DebugDao
import com.jcoronado.minimalbitcoinwidget.DebugLog

private const val LOG_TAG = "PriceViewModel"

class PriceViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: PriceRepository = PriceRepository(application),
    private val debugDao: DebugDao = AppDatabase.getInstance(application).debugDao()
) : AndroidViewModel(application) {

    private fun logApp(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            debugDao.insert(DebugLog(message = message, type = DebugLog.TYPE_APP))
        }
    }

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
        
        // Publish or refresh Glance widget preview for the system widget picker on Android 15+
        updateWidgetPreview(application)

        // Trigger a fresh price fetch using the newly loaded currency preference.
        fetchPrice(fromInit = true)
    }

    /** Load initial data from Repository/SharedPreferences. */
    private fun loadInitialData() {
        val showSparkline = prefs.getBoolean(Prefs.SHOW_SPARKLINE, true)

        if (repository.isMockUiEnabled()) {
            val mockData = repository.getMockPriceData()
            val mockCurrency = repository.getMockCurrency()
            val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, AppConstants.CHANGE_PERCENTAGE_DEFAULT)
            val interval = TimeInterval.fromValue(selectedInterval)

            _uiState.value = PriceUiState(
                price = mockData.currentPrice,
                percentageChange = mockData.getPercentageForInterval(selectedInterval),
                isLoading = false,
                selectedCurrency = mockCurrency,
                changeIntervalLabelResId = interval.labelResId,
                lastUpdated = 0L,
                sparklinePrices = mockData.getSparklineForInterval(selectedInterval),
                showSparkline = showSparkline
            )
            return
        }

        val cachedCurrency = repository.getSelectedCurrency()
        val cachedData = repository.getCachedPriceData()
        if (cachedData != null) {
            try {
                val lastUpdated = repository.getLastApiCallTimestamp()
                val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, AppConstants.CHANGE_PERCENTAGE_DEFAULT)
                val percentage = cachedData.getPercentageForInterval(selectedInterval)

                val interval = TimeInterval.fromValue(selectedInterval)

                // update state immediately
                _uiState.value = _uiState.value.copy(
                    price = cachedData.currentPrice,
                    percentageChange = percentage,
                    changeIntervalLabelResId = interval.labelResId,
                    selectedCurrency = cachedCurrency,
                    lastUpdated = lastUpdated,
                    sparklinePrices = cachedData.getSparklineForInterval(selectedInterval),
                    showSparkline = showSparkline
                )
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to parse initial cache $e")
                _uiState.value = _uiState.value.copy(selectedCurrency = cachedCurrency, showSparkline = showSparkline)
            }
        } else {
            _uiState.value = _uiState.value.copy(selectedCurrency = cachedCurrency, showSparkline = showSparkline)
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
                errorMessage = null,
                selectedCurrency = currentState.selectedCurrency
            )

            val fontKey = prefs.getString(Prefs.SELECTED_WIDGET_FONT, WidgetFont.DEFAULT.key) ?: WidgetFont.DEFAULT.key
            val priceSizeKey = prefs.getString(Prefs.WIDGET_PRICE_SIZE, "default") ?: "default"
            val colorStyleKey = prefs.getString(Prefs.WIDGET_COLOR_STYLE, "dynamic") ?: "dynamic"
            val themeKey = prefs.getString(Prefs.WIDGET_THEME, "system") ?: "system"

            try {
                GlanceAppWidgetManager(getApplication()).requestPinGlanceAppWidget(
                    receiver = PriceWidgetReceiver::class.java,
                    preview = PriceWidget(),
                    previewState = PriceWidgetState.Available(
                        price = currentState.price,
                        changePercentage = currentState.percentageChange,
                        intervalLabelResId = currentState.changeIntervalLabelResId,
                        currency = currentState.selectedCurrency,
                        fontKey = fontKey,
                        priceSizeKey = priceSizeKey,
                        colorStyleKey = colorStyleKey,
                        themeKey = themeKey
                    )
                )
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to request pin widget: $e")
            }
        }
    }

    /**
     * Requests the system to pin the Minimal Glance widget to the homescreen.
     */
    fun requestPinMinimalWidget() {
        requestPinWidget()
    }

    /**
     * Requests the system to pin the Chart Glance widget to the homescreen.
     */
    fun requestPinChartWidget() {
        viewModelScope.launch {
            Log.d(LOG_TAG, "Requesting to pin Chart Glance widget")
            var currentState = uiState.value

            currentState = PriceUiState(
                isLoading = false,
                changeIntervalLabelResId = currentState.changeIntervalLabelResId,
                percentageChange = if (currentState.percentageChange == 0.0) 2.03 else currentState.percentageChange,
                price = if (currentState.price == 0.0) 52849.10 else currentState.price,
                errorMessage = null,
                selectedCurrency = currentState.selectedCurrency,
                sparklinePrices = if (currentState.changeIntervalLabelResId == com.jcoronado.minimalbitcoinwidget.R.string.interval_30d) emptyList() else if (currentState.sparklinePrices.isNotEmpty()) currentState.sparklinePrices else com.jcoronado.minimalbitcoinwidget.ui.components.PLACEHOLDER_SPARKLINE_PRICES
            )

            val fontKey = prefs.getString(Prefs.SELECTED_WIDGET_FONT, WidgetFont.DEFAULT.key) ?: WidgetFont.DEFAULT.key
            val priceSizeKey = prefs.getString(Prefs.WIDGET_PRICE_SIZE, "default") ?: "default"
            val colorStyleKey = prefs.getString(Prefs.WIDGET_COLOR_STYLE, "dynamic") ?: "dynamic"
            val themeKey = prefs.getString(Prefs.WIDGET_THEME, "system") ?: "system"

            try {
                GlanceAppWidgetManager(getApplication()).requestPinGlanceAppWidget(
                    receiver = ChartPriceWidgetReceiver::class.java,
                    preview = ChartPriceWidget(),
                    previewSize = ChartPriceWidget.MEDIUM,
                    previewState = PriceWidgetState.Available(
                        price = currentState.price,
                        changePercentage = currentState.percentageChange,
                        intervalLabelResId = currentState.changeIntervalLabelResId,
                        currency = currentState.selectedCurrency,
                        fontKey = fontKey,
                        priceSizeKey = priceSizeKey,
                        colorStyleKey = colorStyleKey,
                        themeKey = themeKey,
                        sparklinePrices = currentState.sparklinePrices
                    )
                )
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to request pin chart widget: $e")
            }
        }
    }

    /** Update the selected currency and persist it via Repository. */
    fun updateCurrency(newCurrency: String) {
        if (_uiState.value.selectedCurrency == newCurrency) return

        Log.i(LOG_TAG, "Updating currency to: $newCurrency")
        logApp("Currency changed to $newCurrency")

        _uiState.value = _uiState.value.copy(selectedCurrency = newCurrency)
        repository.updateSelectedCurrency(newCurrency)
        fetchPrice(force = true)
    }

    /** Fetch data via Repository. */
    fun fetchPrice(force: Boolean = false, fromInit: Boolean = false, isManual: Boolean = false) {
        if (isManual) {
            logApp("Manual refresh requested")
        } else if (fromInit) {
            logApp("Initial fetch requested")
        }

        if (repository.isMockUiEnabled()) {
            logApp("Mock UI enabled, bypassing fetch")
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

            val isCacheHit = !force && repository.isCacheFresh() && repository.getCachedPriceData() != null
            val currency = _uiState.value.selectedCurrency
            val resource = repository.fetchPrice(currency, force = force)

            delay(750)

            when (resource) {
                is Resource.Success -> {
                    val priceData = resource.data
                    val formattedPrice = FormatUtils.formatPrice(priceData.currentPrice, currency)
                    if (isCacheHit) {
                        logApp("Loaded from cache ($formattedPrice)")
                    } else {
                        logApp("Data fetched ($formattedPrice)")
                    }
                    val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, AppConstants.CHANGE_PERCENTAGE_DEFAULT)
                    val percentage = priceData.getPercentageForInterval(selectedInterval)
                    val interval = TimeInterval.fromValue(selectedInterval)
                    val lastUpdated = repository.getLastApiCallTimestamp()

                    _uiState.value = _uiState.value.copy(
                        price = priceData.currentPrice,
                        percentageChange = percentage,
                        changeIntervalLabelResId = interval.labelResId,
                        isLoading = false,
                        errorMessage = null,
                        lastUpdated = lastUpdated,
                        sparklinePrices = priceData.getSparklineForInterval(selectedInterval)
                    )
                    redrawWidgets()
                }
                is Resource.Error -> {
                    logApp("Failed - ${resource.message}")
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
        updateWidgetPreview(getApplication())
    }

    companion object {
        const val LEGACY_WIDGET_WRAPPER_CLASS = "com.jcoronado.minimalbitcoinwidget.PriceWidget"

        /**
         * Re-reads the cached price data and updates all widget instances (Glance and Legacy).
         * This suspend function allows callers to await full widget dispatch before finishing background tasks.
         */
        suspend fun refreshWidgetsFromCacheSuspend(context: Context) {
            val repository = PriceRepository(context)
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val fontKey = prefs.getString(Prefs.SELECTED_WIDGET_FONT, WidgetFont.DEFAULT.key) ?: WidgetFont.DEFAULT.key
            val priceSizeKey = prefs.getString(Prefs.WIDGET_PRICE_SIZE, "default") ?: "default"
            val colorStyleKey = prefs.getString(Prefs.WIDGET_COLOR_STYLE, "dynamic") ?: "dynamic"
            val themeKey = prefs.getString(Prefs.WIDGET_THEME, "system") ?: "system"

            if (repository.isMockUiEnabled()) {
                val mockData = repository.getMockPriceData()
                val mockCurrency = repository.getMockCurrency()
                val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, AppConstants.CHANGE_PERCENTAGE_DEFAULT)
                val interval = TimeInterval.fromValue(selectedInterval)

                val glanceState = PriceWidgetState.Available(
                    price = mockData.currentPrice,
                    changePercentage = mockData.getPercentageForInterval(selectedInterval),
                    intervalLabelResId = interval.labelResId,
                    currency = mockCurrency,
                    fontKey = fontKey,
                    priceSizeKey = priceSizeKey,
                    colorStyleKey = colorStyleKey,
                    themeKey = themeKey,
                    sparklinePrices = mockData.getSparklineForInterval(selectedInterval)
                )

                updateGlanceWidgets(context, glanceState)
                updateLegacyWidgets(context, mockData, mockCurrency)
                return
            }

            val priceData = repository.getCachedPriceData() ?: return
            val currencyCode = repository.getSelectedCurrency()

            val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, AppConstants.CHANGE_PERCENTAGE_DEFAULT)
            val percentage = priceData.getPercentageForInterval(selectedInterval)
            val interval = TimeInterval.fromValue(selectedInterval)

            val glanceState = PriceWidgetState.Available(
                price = priceData.currentPrice,
                changePercentage = percentage,
                intervalLabelResId = interval.labelResId,
                currency = currencyCode,
                fontKey = fontKey,
                priceSizeKey = priceSizeKey,
                colorStyleKey = colorStyleKey,
                themeKey = themeKey,
                sparklinePrices = priceData.getSparklineForInterval(selectedInterval)
            )

            updateGlanceWidgets(context, glanceState)
            updateLegacyWidgets(context, priceData)
        }

        /**
         * Fire-and-forget version of refreshWidgetsFromCache for standard UI callers.
         */
        fun refreshWidgetsFromCache(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                refreshWidgetsFromCacheSuspend(context)
            }
        }

        private var lastPreviewUpdateTime = 0L
        private var lastPreviewFingerprint: String? = null
        private const val PREVIEW_UPDATE_COOLDOWN_MS = 60 * 60 * 1000L // 1 hour cooldown to respect Android's ~2 calls/hour rate limit

        /**
         * Generates and publishes the Glance widget preview for the system widget picker
         * on Android 15+ (API 35+).
         * Strictly throttled to 1 hour and skips when styling fingerprint has not changed,
         * ensuring the OS rate limit (~2 calls/hour) is never exceeded.
         */
        fun updateWidgetPreview(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                val now = System.currentTimeMillis()
                if ((now - lastPreviewUpdateTime) < PREVIEW_UPDATE_COOLDOWN_MS) {
                    val remainingSeconds = (PREVIEW_UPDATE_COOLDOWN_MS - (now - lastPreviewUpdateTime)) / 1000
                    Log.d(LOG_TAG, "Skipping widget preview update (throttled, cooldown active: ${remainingSeconds}s remaining)")
                    return
                }

                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val fontKey = prefs.getString(Prefs.SELECTED_WIDGET_FONT, WidgetFont.DEFAULT.key) ?: WidgetFont.DEFAULT.key
                val priceSizeKey = prefs.getString(Prefs.WIDGET_PRICE_SIZE, "default") ?: "default"
                val colorStyleKey = prefs.getString(Prefs.WIDGET_COLOR_STYLE, "dynamic") ?: "dynamic"
                val themeKey = prefs.getString(Prefs.WIDGET_THEME, "system") ?: "system"
                val currentFingerprint = "$fontKey:$priceSizeKey:$colorStyleKey:$themeKey"

                if (currentFingerprint == lastPreviewFingerprint && lastPreviewUpdateTime > 0L) {
                    Log.d(LOG_TAG, "Skipping widget preview update (styling fingerprint unchanged)")
                    return
                }

                lastPreviewUpdateTime = now
                lastPreviewFingerprint = currentFingerprint
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val manager = GlanceAppWidgetManager(context)
                        val pResult = manager.setWidgetPreviews(PriceWidgetReceiver::class)
                        val tResult = manager.setWidgetPreviews(ChartPriceWidgetReceiver::class)
                        Log.d(LOG_TAG, "setWidgetPreviews PriceWidget: $pResult, Chart: $tResult")
                    } catch (e: Exception) {
                        Log.w(LOG_TAG, "Failed to update widget preview: $e")
                    }
                }
            }
        }

        /**
         * Updates all Glance widgets with the given state.
         */
        suspend fun updateGlanceWidgets(context: Context, state: PriceWidgetState) {
            val manager = GlanceAppWidgetManager(context)

            // PriceWidget
            val priceGlanceIds = manager.getGlanceIds(PriceWidget::class.java)
            priceGlanceIds.forEach { glanceId ->
                updateAppWidgetState(context, PriceWidgetStateDefinition, glanceId) {
                    state
                }
            }
            PriceWidget().updateAll(context)

            // Chart Widget
            val chartGlanceIds = manager.getGlanceIds(ChartPriceWidget::class.java)
            chartGlanceIds.forEach { glanceId ->
                updateAppWidgetState(context, PriceWidgetStateDefinition, glanceId) {
                    state
                }
            }
            ChartPriceWidget().updateAll(context)
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


package com.jcoronado.minimalbitcoinwidget.viewmodels

import android.app.Application
import android.os.Build
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import com.jcoronado.minimalbitcoinwidget.classes.AppConstants
import com.jcoronado.minimalbitcoinwidget.classes.Prefs
import com.jcoronado.minimalbitcoinwidget.classes.WidgetFont
import com.jcoronado.minimalbitcoinwidget.workers.PriceUpdateWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)
    private val _theme = MutableStateFlow(getSavedTheme())
    private val _widgetFont = MutableStateFlow(getSavedWidgetFont())
    private val _widgetPriceBold = MutableStateFlow(getSavedWidgetPriceBold())
    private val _dynamicColors = MutableStateFlow(getDynamicColorsFlag())
    private val _refreshInterval = MutableStateFlow(getSavedRefreshInterval())
    private val _changePercentageInterval = MutableStateFlow(getSavedChangePercentageInterval())
    private val _showSparkline = MutableStateFlow(getShowSparkline())
    private val _developerModeEnabled = MutableStateFlow(getDeveloperModeEnabled())

    val theme: StateFlow<AppTheme> = _theme.asStateFlow()
    val widgetFont: StateFlow<WidgetFont> = _widgetFont.asStateFlow()
    val widgetPriceBold: StateFlow<Boolean> = _widgetPriceBold.asStateFlow()
    val dynamicColors: StateFlow<Boolean> = _dynamicColors.asStateFlow()
    val refreshInterval: StateFlow<Int> = _refreshInterval.asStateFlow()
    val changePercentageInterval: StateFlow<Int> = _changePercentageInterval.asStateFlow()
    val showSparkline: StateFlow<Boolean> = _showSparkline.asStateFlow()
    val developerModeEnabled: StateFlow<Boolean> = _developerModeEnabled.asStateFlow()

    private val _debugMockUiEnabled = MutableStateFlow(getDebugMockUiEnabled())
    private val _debugMockPrice = MutableStateFlow(getDebugMockPrice())
    private val _debugMockPercentChange = MutableStateFlow(getDebugMockPercentChange())
    private val _debugMockCurrency = MutableStateFlow(getDebugMockCurrency())

    val debugMockUiEnabled: StateFlow<Boolean> = _debugMockUiEnabled.asStateFlow()
    val debugMockPrice: StateFlow<String> = _debugMockPrice.asStateFlow()
    val debugMockPercentChange: StateFlow<String> = _debugMockPercentChange.asStateFlow()
    val debugMockCurrency: StateFlow<String> = _debugMockCurrency.asStateFlow()

    private fun getSavedTheme(): AppTheme {
        val themeName = prefs.getString(Prefs.SELECTED_THEME, AppTheme.SYSTEM.name)
        return AppTheme.valueOf(themeName ?: AppTheme.SYSTEM.name)
    }

    fun setTheme(newTheme: AppTheme) {
        _theme.value = newTheme
        prefs.edit { putString(Prefs.SELECTED_THEME, newTheme.name) }
    }

    private fun getSavedWidgetFont(): WidgetFont {
        val fontKey = prefs.getString(Prefs.SELECTED_WIDGET_FONT, WidgetFont.DEFAULT.key)
        return WidgetFont.fromKey(fontKey)
    }

    private fun getSavedWidgetPriceBold(): Boolean {
        return prefs.getBoolean(Prefs.WIDGET_PRICE_BOLD, false)
    }

    fun setWidgetFont(newFont: WidgetFont) {
        _widgetFont.value = newFont
        prefs.edit(commit = true) { putString(Prefs.SELECTED_WIDGET_FONT, newFont.key) }
        PriceViewModel.refreshWidgetsFromCache(getApplication())
    }

    fun setWidgetPriceBold(isBold: Boolean) {
        _widgetPriceBold.value = isBold
        prefs.edit(commit = true) { putBoolean(Prefs.WIDGET_PRICE_BOLD, isBold) }
        PriceViewModel.refreshWidgetsFromCache(getApplication())
    }

    fun saveWidgetCustomization(newFont: WidgetFont, isBold: Boolean) {
        _widgetFont.value = newFont
        _widgetPriceBold.value = isBold
        prefs.edit(commit = true) {
            putString(Prefs.SELECTED_WIDGET_FONT, newFont.key)
            putBoolean(Prefs.WIDGET_PRICE_BOLD, isBold)
        }
        PriceViewModel.refreshWidgetsFromCache(getApplication())
    }

    private fun getDynamicColorsFlag(): Boolean {
        return prefs.getBoolean(Prefs.DYNAMIC_COLORS, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    }

    fun updateDynamicColorsFlag(toggleValue: Boolean) {
        _dynamicColors.value = toggleValue
        prefs.edit { putBoolean(Prefs.DYNAMIC_COLORS, toggleValue) }
    }

    private fun getSavedRefreshInterval(): Int {
        return prefs.getInt(Prefs.REFRESH_INTERVAL, 1)
    }

    fun setRefreshInterval(index: Int) {
        _refreshInterval.value = index
        prefs.edit { putInt(Prefs.REFRESH_INTERVAL, index) }
        PriceUpdateWorker.enqueue(getApplication())
    }

    private fun getSavedChangePercentageInterval(): Int {
        return prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, 0)
    }

    fun setChangePercentageInterval(index: Int) {
        _changePercentageInterval.value = index
        prefs.edit { putInt(Prefs.SELECTED_CHANGE_PERCENTAGE, index) }
        
        // update all widgets immediately using helper in PriceViewModel
        PriceViewModel.refreshWidgetsFromCache(getApplication())
    }

    private fun getShowSparkline(): Boolean {
        return prefs.getBoolean(Prefs.SHOW_SPARKLINE, true)
    }

    fun setShowSparkline(enabled: Boolean) {
        _showSparkline.value = enabled
        prefs.edit { putBoolean(Prefs.SHOW_SPARKLINE, enabled) }
    }

    private fun getDeveloperModeEnabled(): Boolean {
        return prefs.getBoolean(Prefs.DEVELOPER_MODE_ENABLED, false)
    }

    fun setDeveloperModeEnabled(enabled: Boolean) {
        _developerModeEnabled.value = enabled
        prefs.edit { putBoolean(Prefs.DEVELOPER_MODE_ENABLED, enabled) }
    }

    private fun getDebugMockUiEnabled(): Boolean {
        return prefs.getBoolean(Prefs.DEBUG_MOCK_UI_ENABLED, false)
    }

    fun setDebugMockUiEnabled(enabled: Boolean) {
        _debugMockUiEnabled.value = enabled
        prefs.edit { putBoolean(Prefs.DEBUG_MOCK_UI_ENABLED, enabled) }
    }

    private fun getDebugMockPrice(): String {
        return prefs.getString(Prefs.DEBUG_MOCK_PRICE, AppConstants.DEBUG_MOCK_PRICE_DEFAULT) ?: AppConstants.DEBUG_MOCK_PRICE_DEFAULT
    }

    fun setDebugMockPrice(price: String) {
        _debugMockPrice.value = price
        prefs.edit { putString(Prefs.DEBUG_MOCK_PRICE, price) }
    }

    private fun getDebugMockPercentChange(): String {
        return prefs.getString(Prefs.DEBUG_MOCK_PERCENT_CHANGE, AppConstants.DEBUG_MOCK_PERCENT_CHANGE_DEFAULT) ?: AppConstants.DEBUG_MOCK_PERCENT_CHANGE_DEFAULT
    }

    fun setDebugMockPercentChange(percentChange: String) {
        _debugMockPercentChange.value = percentChange
        prefs.edit { putString(Prefs.DEBUG_MOCK_PERCENT_CHANGE, percentChange) }
    }

    private fun getDebugMockCurrency(): String {
        return prefs.getString(Prefs.DEBUG_MOCK_CURRENCY, AppConstants.DEBUG_MOCK_CURRENCY_DEFAULT) ?: AppConstants.DEBUG_MOCK_CURRENCY_DEFAULT
    }

    fun setDebugMockCurrency(currency: String) {
        _debugMockCurrency.value = currency
        prefs.edit { putString(Prefs.DEBUG_MOCK_CURRENCY, currency) }
    }
}

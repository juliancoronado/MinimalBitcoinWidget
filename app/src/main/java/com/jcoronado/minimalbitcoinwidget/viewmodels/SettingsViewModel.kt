package com.jcoronado.minimalbitcoinwidget.viewmodels

import android.app.Application
import android.os.Build
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import com.jcoronado.minimalbitcoinwidget.classes.Prefs
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
    private val _dynamicColors = MutableStateFlow(getDynamicColorsFlag())
    private val _refreshInterval = MutableStateFlow(getSavedRefreshInterval())
    private val _changePercentageInterval = MutableStateFlow(getSavedChangePercentageInterval())
    private val _developerModeEnabled = MutableStateFlow(getDeveloperModeEnabled())

    val theme: StateFlow<AppTheme> = _theme.asStateFlow()
    val dynamicColors: StateFlow<Boolean> = _dynamicColors.asStateFlow()
    val refreshInterval: StateFlow<Int> = _refreshInterval.asStateFlow()
    val changePercentageInterval: StateFlow<Int> = _changePercentageInterval.asStateFlow()
    val developerModeEnabled: StateFlow<Boolean> = _developerModeEnabled.asStateFlow()

    private fun getSavedTheme(): AppTheme {
        val themeName = prefs.getString(Prefs.SELECTED_THEME, AppTheme.SYSTEM.name)
        return AppTheme.valueOf(themeName ?: AppTheme.SYSTEM.name)
    }

    fun setTheme(newTheme: AppTheme) {
        _theme.value = newTheme
        prefs.edit { putString(Prefs.SELECTED_THEME, newTheme.name) }
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

    private fun getDeveloperModeEnabled(): Boolean {
        return prefs.getBoolean(Prefs.DEVELOPER_MODE_ENABLED, false)
    }

    fun setDeveloperModeEnabled(enabled: Boolean) {
        _developerModeEnabled.value = enabled
        prefs.edit { putBoolean(Prefs.DEVELOPER_MODE_ENABLED, enabled) }
    }
}

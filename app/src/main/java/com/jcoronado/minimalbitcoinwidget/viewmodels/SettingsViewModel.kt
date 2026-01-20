package com.jcoronado.minimalbitcoinwidget.viewmodels

import android.app.Application
import android.os.Build
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import com.jcoronado.minimalbitcoinwidget.classes.Prefs
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

    val theme: StateFlow<AppTheme> = _theme.asStateFlow()
    val dynamicColors: StateFlow<Boolean> = _dynamicColors.asStateFlow()

    private fun getSavedTheme(): AppTheme {
        val themeName = prefs.getString(Prefs.SELECTED_THEME, AppTheme.SYSTEM.name)
        return AppTheme.valueOf(themeName ?: AppTheme.SYSTEM.name)
    }

    fun setTheme(newTheme: AppTheme) {
        // update UI state immediately and then save to SharedPrefs
        _theme.value = newTheme
        prefs.edit { putString(Prefs.SELECTED_THEME, newTheme.name) }
    }

    private fun getDynamicColorsFlag(): Boolean {
        return prefs.getBoolean(Prefs.DYNAMIC_COLORS, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    }

    fun updateDynamicColorsFlag(toggleValue: Boolean) {
        // update UI state immediately and then save to SharedPrefs
        _dynamicColors.value = toggleValue
        prefs.edit { putBoolean(Prefs.DYNAMIC_COLORS, toggleValue) }
    }
}
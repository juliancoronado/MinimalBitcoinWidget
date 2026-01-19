package com.jcoronado.minimalbitcoinwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jcoronado.minimalbitcoinwidget.screens.AppNavigation
import com.jcoronado.minimalbitcoinwidget.ui.theme.AppTheme
import com.jcoronado.minimalbitcoinwidget.viewmodels.AppTheme
import com.jcoronado.minimalbitcoinwidget.viewmodels.SettingsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel : SettingsViewModel =  viewModel()
            val themePreference by settingsViewModel.theme.collectAsStateWithLifecycle()
            val useDynamicColors by settingsViewModel.dynamicColors.collectAsStateWithLifecycle()

            val useDarkTheme = when (themePreference) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            AppTheme(darkTheme = useDarkTheme, dynamicColors = useDynamicColors) {
                AppNavigation()
            }
        }
    }
}
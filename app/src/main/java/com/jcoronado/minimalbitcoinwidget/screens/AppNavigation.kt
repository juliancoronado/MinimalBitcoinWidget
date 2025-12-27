package com.jcoronado.minimalbitcoinwidget.screens

import android.content.Intent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.jcoronado.minimalbitcoinwidget.viewmodels.PriceViewModel
import kotlinx.serialization.Serializable

@Serializable
data object MainScreen : NavKey

@Serializable
data object SettingsScreen : NavKey

@Composable
fun AppNavigation(
    intent: Intent? = null
) {
    val backStack = remember { mutableStateListOf<Any>(MainScreen) }

    LaunchedEffect(intent) {
        // if the app is opened from the widget, clear the current stack
        if (intent?.action == Intent.ACTION_MAIN || intent?.hasExtra("reset_nav") == true) {
            if (backStack.size > 1) {
                backStack.clear()
                backStack.add(MainScreen)
            }
        }
    }

    NavDisplay(
        backStack = backStack, onBack = { backStack.removeLastOrNull() },
        // these transition specs will do for now
        transitionSpec = {
            (slideInHorizontally(initialOffsetX = { it }) + fadeIn(initialAlpha = 0.5F)).togetherWith(
                slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut(
                    targetAlpha = 0.75F
                )
            )
        }, popTransitionSpec = {
            (slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn(initialAlpha = 0.5F)).togetherWith(
                slideOutHorizontally(targetOffsetX = { it })
            )
        }, predictivePopTransitionSpec = {
            (slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn(initialAlpha = 0.5F)).togetherWith(
                slideOutHorizontally(targetOffsetX = { it })
            )
        }, entryProvider = { key ->
            when (key) {
                is MainScreen -> {
                    NavEntry(key) {
                        val viewModel: PriceViewModel = viewModel()
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        MainScreen(
                            uiState = uiState,
                            onSettingsClick = { backStack.add(SettingsScreen) },
                            onRefresh = { viewModel.fetchPrice() },
                        )
                    }
                }

                is SettingsScreen -> {
                    NavEntry(
                        key
                    ) {
                        val viewModel: PriceViewModel = viewModel()
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        SettingsScreen(
                            selectedCurrency = uiState.selectedCurrency,
                            onCurrencySelected = { viewModel.updateCurrency(it) },
                            onBackButtonClick = { backStack.removeAt(backStack.lastIndex) })
                    }
                }

                else -> error("Unknown key: $key")
            }
        })
}

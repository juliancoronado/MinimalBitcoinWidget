package com.jcoronado.minimalbitcoinwidget.screens

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
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
) {
    val backStack = remember { mutableStateListOf<Any>(MainScreen) }
    NavDisplay(
        backStack = backStack, onBack = { backStack.removeLastOrNull() },
        // these transition specs will do for now, but
        // TODO - match this as close as possible to Android 16 default animation
        transitionSpec = {
            slideInHorizontally {
                it / 3
            } + fadeIn() togetherWith slideOutHorizontally {
                -it / 3
            } + fadeOut()
        },
        popTransitionSpec = {
            slideInHorizontally {
                - it / 3
            } + fadeIn() togetherWith slideOutHorizontally {
                it / 3
            } + fadeOut()
        },
        predictivePopTransitionSpec = {
            slideInHorizontally {
                - it / 3
            } + fadeIn() togetherWith slideOutHorizontally {
                it / 3
            } + fadeOut()
        },
        entryProvider = { key ->
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
                            onBackButtonClick = { backStack.removeAt(backStack.lastIndex) }
                        )
                    }
                }

                else -> error("Unknown key: $key")
            }
        })
}

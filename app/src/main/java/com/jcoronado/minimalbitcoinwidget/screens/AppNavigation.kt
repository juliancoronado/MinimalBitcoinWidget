package com.jcoronado.minimalbitcoinwidget.screens

import androidx.compose.animation.core.tween
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
        transitionSpec = {
            // screen forward animation
            val enterTransition = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(300)
            ) + fadeIn(tween(250))

            enterTransition togetherWith fadeOut(tween(500))
        },
        popTransitionSpec = {
            // screen back animation
            val exitTransition = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(300)
            ) + fadeOut(tween(500))
            fadeIn(animationSpec = tween(500)) togetherWith exitTransition
        },
        predictivePopTransitionSpec = {
            // predictive back animation
            val enterTransition = slideInHorizontally(
                initialOffsetX = { -it / 2 }, animationSpec = tween(500)
            ) + fadeIn(tween(500))
            val exitTransition = slideOutHorizontally(
                targetOffsetX = { it / 2 }, animationSpec = tween(500)
            ) + fadeOut(tween(500))
            enterTransition togetherWith exitTransition
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
                        SettingsScreen(
                            onBackButtonClick = { backStack.removeAt(backStack.lastIndex) })
                    }
                }

                else -> error("Unknown key: $key")
            }
        })
}
package com.jcoronado.minimalbitcoinwidget.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.NavItem
import com.jcoronado.minimalbitcoinwidget.classes.Screen
import com.jcoronado.minimalbitcoinwidget.viewmodels.PriceViewModel

val navigationScreenList = listOf(
    NavItem(
        route = Screen.Dashboard, icon = R.drawable.rounded_dashboard_24, label = R.string.dashboard
    ), NavItem(
        Screen.Settings.Main, R.drawable.rounded_settings_24, R.string.settings
    )
)

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
    androidx.compose.animation.ExperimentalSharedTransitionApi::class
)
@Composable
fun AppNavigation() {
    val systemBarsInsets = WindowInsets.systemBars.asPaddingValues()
    val cutoutInsets = WindowInsets.displayCutout.asPaddingValues()

    val toolbarScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        FloatingToolbarExitDirection.Bottom
    )

    val motionScheme = motionScheme
    val backStack = rememberNavBackStack(Screen.Dashboard)
    val viewModel: PriceViewModel = viewModel()

    // Show NavBar only on top-level routes (Dashboard or Settings Main)
    val isNavBarVisible by remember {
        derivedStateOf {
            val current = backStack.lastOrNull()
            current == Screen.Dashboard || current == Screen.Settings.Main
        }
    }

    Scaffold(bottomBar = {
        AnimatedVisibility(
            enter = slideInVertically(motionScheme.slowSpatialSpec()) { it },
            exit = slideOutVertically(motionScheme.slowSpatialSpec()) { it },
            visible = isNavBarVisible
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = cutoutInsets.calculateStartPadding(LocalLayoutDirection.current),
                        end = cutoutInsets.calculateEndPadding(LocalLayoutDirection.current)
                    ), Alignment.Center
            ) {
                HorizontalFloatingToolbar(
                    expanded = true,
                    scrollBehavior = toolbarScrollBehavior,
                    modifier = Modifier
                        .padding(
                            top = ScreenOffset,
                            bottom = systemBarsInsets.calculateBottomPadding() + ScreenOffset
                        )
                        .zIndex(1f)
                ) {
                    navigationScreenList.fastForEach { item ->
                        val selected by remember {
                            derivedStateOf {
                                val current = backStack.lastOrNull()
                                if (item.route == Screen.Dashboard) {
                                    current == Screen.Dashboard
                                } else {
                                    current is Screen.Settings
                                }
                            }
                        }
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                TooltipAnchorPosition.Above
                            ),
                            tooltip = { PlainTooltip { Text(stringResource(item.label)) } },
                            state = rememberTooltipState(),
                        ) {
                            ToggleButton(
                                checked = selected, onCheckedChange = {
                                    if (item.route != Screen.Dashboard) {
                                        // If we are deep in settings, go back to Settings Main
                                        // Otherwise, just add Settings Main
                                        if (backStack.any { it is Screen.Settings }) {
                                            while (backStack.lastOrNull() !is Screen.Settings.Main) {
                                                backStack.removeLastOrNull()
                                            }
                                        } else {
                                            backStack.add(item.route)
                                        }
                                    } else {
                                        // Reset to Dashboard
                                        while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                                    }
                                }, shapes = ToggleButtonDefaults.shapes(
                                    CircleShape, CircleShape, CircleShape
                                ), modifier = Modifier.height(56.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painterResource(item.icon), stringResource(item.label)
                                    )
                                    AnimatedVisibility(
                                        visible = selected,
                                        enter = expandHorizontally(motionScheme.defaultSpatialSpec()),
                                        exit = shrinkHorizontally(motionScheme.defaultSpatialSpec())
                                    ) {
                                        Text(
                                            text = stringResource(item.label),
                                            fontSize = 16.sp,
                                            lineHeight = 24.sp,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Clip,
                                            modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }) { contentPadding ->
        val silencePaddingWarning = contentPadding
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) backStack.removeLastOrNull()
            },
            transitionSpec = {
                val to = backStack.lastOrNull()
                if (to is Screen.Settings && to != Screen.Settings.Main) {
                    // Slide in for inner settings
                    (slideInHorizontally(initialOffsetX = { it }) + fadeIn(initialAlpha = 0.5F)).togetherWith(
                        slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut(targetAlpha = 0.75F)
                    )
                } else {
                    // Default fade for tabs
                    fadeIn(motionScheme.defaultEffectsSpec()).togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                }
            },
            popTransitionSpec = {
                val from = backStack.lastOrNull()
                if (from is Screen.Settings) {
                    (slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn(initialAlpha = 0.5F)).togetherWith(
                        slideOutHorizontally(targetOffsetX = { it })
                    )
                } else {
                    fadeIn(motionScheme.defaultEffectsSpec()).togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                }
            },
            predictivePopTransitionSpec = {
                val from = backStack.lastOrNull()
                if (from is Screen.Settings) {
                    (slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn(initialAlpha = 0.5F)).togetherWith(
                        slideOutHorizontally(targetOffsetX = { it })
                    )
                } else {
                    fadeIn(motionScheme.defaultEffectsSpec()).togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                }
            },
            entryProvider = entryProvider {
                entry<Screen.Dashboard> {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    MainScreen(
                        uiState = uiState,
                        onRefresh = { viewModel.fetchPrice() },
                    )
                }
                entry<Screen.Settings.Main> {
                    SettingsScreen(
                        onNavigate = { route -> backStack.add(route) })
                }
                entry<Screen.Settings.Data> {
                    DataSettingsScreen(onBack = { backStack.removeAt(backStack.lastIndex) })
                }
                entry<Screen.Settings.Appearance> {
                    AppearanceSettingsScreen(onBack = { backStack.removeAt(backStack.lastIndex) })
                }
                entry<Screen.Settings.About> {
                    AboutScreen(onBack = { backStack.removeAt(backStack.lastIndex) })
                }
            },
        )
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun NavigationPreview() {
    AppNavigation()
}

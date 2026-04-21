package com.jcoronado.minimalbitcoinwidget.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.jcoronado.minimalbitcoinwidget.viewmodels.SettingsViewModel

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun AppNavigation() {
    val systemBarsInsets = WindowInsets.systemBars.asPaddingValues()
    val cutoutInsets = WindowInsets.displayCutout.asPaddingValues()

    val motionScheme = MaterialTheme.motionScheme
    val backStack = rememberNavBackStack(Screen.Dashboard)
    val priceViewModel: PriceViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val uiState by priceViewModel.uiState.collectAsStateWithLifecycle()
    val developerModeEnabled by settingsViewModel.developerModeEnabled.collectAsStateWithLifecycle()

    val currentRoute by remember(backStack.size) {
        derivedStateOf { backStack.lastOrNull() }
    }

    val showNavBar by remember(currentRoute) {
        derivedStateOf {
            currentRoute == Screen.Dashboard || currentRoute == Screen.Settings || currentRoute == Screen.DeveloperOptions
        }
    }

    var isNavBarVisibleByScroll by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -1f) {
                    isNavBarVisibleByScroll = false
                } else if (available.y > 1f) {
                    isNavBarVisibleByScroll = true
                }
                return Offset.Zero
            }
        }
    }

    var navBarSelectedItem by remember { mutableStateOf(currentRoute) }

    LaunchedEffect(currentRoute) {
        isNavBarVisibleByScroll = true
        if (showNavBar) {
            navBarSelectedItem = currentRoute
        }
    }

    val navigationScreenList = remember(developerModeEnabled) {
        listOfNotNull(
            NavItem(
                route = Screen.Dashboard,
                icon = R.drawable.rounded_dashboard_24,
                label = R.string.dashboard
            ), NavItem(
                route = Screen.Settings,
                icon = R.drawable.rounded_settings_24,
                label = R.string.settings
            ), if (developerModeEnabled) NavItem(
                route = Screen.DeveloperOptions,
                icon = R.drawable.rounded_code_24,
                label = R.string.developer_title
            ) else null
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
                backStack = backStack,
                onBack = {
                    if (backStack.size > 1) backStack.removeLastOrNull()
                },
                transitionSpec = {
                    fadeIn(motionScheme.defaultEffectsSpec()) togetherWith fadeOut(motionScheme.defaultEffectsSpec())
                },
                popTransitionSpec = {
                    fadeIn(motionScheme.defaultEffectsSpec()) togetherWith fadeOut(motionScheme.defaultEffectsSpec())
                },
                predictivePopTransitionSpec = {
                    fadeIn(motionScheme.defaultEffectsSpec()) togetherWith fadeOut(motionScheme.defaultEffectsSpec())
                },
                entryProvider = entryProvider {
                    entry<Screen.Dashboard> {
                        MainScreen(
                            uiState = uiState,
                            onRefresh = { priceViewModel.fetchPrice() },
                        )
                    }
                    entry<Screen.Settings> {
                        val currentTheme by settingsViewModel.theme.collectAsStateWithLifecycle()
                        val dynamicColors by settingsViewModel.dynamicColors.collectAsStateWithLifecycle()
                        val refreshInterval by settingsViewModel.refreshInterval.collectAsStateWithLifecycle()
                        val changePercentage by settingsViewModel.changePercentageInterval.collectAsStateWithLifecycle()

                        SettingsScreen(
                            selectedCurrency = uiState.selectedCurrency,
                            onCurrencySelected = { newCurrency ->
                                priceViewModel.updateCurrency(
                                    newCurrency
                                )
                            },
                            currentTheme = currentTheme,
                            onThemeSelected = { newTheme -> settingsViewModel.setTheme(newTheme) },
                            dynamicColors = dynamicColors,
                            onDynamicColorsSelected = { newDynamicColors ->
                                settingsViewModel.updateDynamicColorsFlag(
                                    newDynamicColors
                                )
                            },
                            refreshInterval = refreshInterval,
                            onRefreshIntervalSelected = { newInterval ->
                                settingsViewModel.setRefreshInterval(newInterval)
                            },
                            changePercentage = changePercentage,
                            onChangePercentageSelected = { newChangePercentage ->
                                settingsViewModel.setChangePercentageInterval(newChangePercentage)
                                priceViewModel.refreshFromCache()
                            },
                            developerModeEnabled = developerModeEnabled,
                            onDebugModeToggle = { enabled ->
                                settingsViewModel.setDeveloperModeEnabled(enabled)
                            })
                    }
                    entry<Screen.DeveloperOptions> {
                        DeveloperOptionsScreen(
                            onNavigateToLogs = { backStack.add(Screen.WidgetLogs) }
                        )
                    }
                    entry<Screen.WidgetLogs> {
                        WidgetLogsScreen(
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                },
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = cutoutInsets.calculateStartPadding(LocalLayoutDirection.current),
                        end = cutoutInsets.calculateEndPadding(LocalLayoutDirection.current)
                    ), Alignment.BottomCenter
            ) {
                AnimatedVisibility(
                    visible = showNavBar && isNavBarVisibleByScroll,
                    enter = fadeIn(motionScheme.defaultSpatialSpec()) + slideInVertically(
                        motionScheme.defaultSpatialSpec()
                    ) { it },
                    exit = fadeOut(motionScheme.defaultSpatialSpec()) + slideOutVertically(
                        motionScheme.defaultSpatialSpec()
                    ) { it },
                ) {
                    HorizontalFloatingToolbar(
                        expanded = true, colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(
                            toolbarContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            toolbarContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ), modifier = Modifier
                            .padding(
                                top = ScreenOffset,
                                bottom = systemBarsInsets.calculateBottomPadding() + ScreenOffset
                            )
                            .zIndex(1f)
                    ) {
                        navigationScreenList.fastForEach { item ->
                            val selected = item.route == navBarSelectedItem
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Above
                                ),
                                tooltip = { PlainTooltip { Text(stringResource(item.label)) } },
                                state = rememberTooltipState(),
                            ) {
                                ToggleButton(
                                    checked = selected, onCheckedChange = { checked ->
                                        if (checked && !selected) {
                                            // Keep only Dashboard in the stack before adding the new route
                                            while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                                            if (item.route != Screen.Dashboard) {
                                                backStack.add(item.route)
                                            }
                                        }
                                    }, colors = ToggleButtonDefaults.toggleButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        checkedContainerColor = MaterialTheme.colorScheme.primary,
                                        checkedContentColor = MaterialTheme.colorScheme.onPrimary
                                    ), shapes = ToggleButtonDefaults.shapes(
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


        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun NavigationPreview() {
    AppNavigation()
}

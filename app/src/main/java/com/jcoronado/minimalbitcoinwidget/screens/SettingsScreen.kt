package com.jcoronado.minimalbitcoinwidget.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jcoronado.minimalbitcoinwidget.BuildConfig
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.viewmodels.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit = {},
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit = {},
    dynamicColors: Boolean,
    onDynamicColorsSelected: (Boolean) -> Unit = {},
    refreshInterval: Int = 1,
    onRefreshIntervalSelected: (Int) -> Unit = {},
    changePercentage: Int = 0,
    onChangePercentageSelected: (Int) -> Unit = {},
    debugModeEnabled: Boolean = false,
    onDebugModeToggle: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val showCurrencyDialog = remember { mutableStateOf(false) }
    var newSelection by remember { mutableStateOf(selectedCurrency) }
    val currencyDescriptions = stringArrayResource(R.array.currency_descriptions)
    val currencyCodes = stringArrayResource(R.array.currency_codes)

    var tapCount by remember { mutableIntStateOf(0) }

    if (showCurrencyDialog.value) {
        AlertDialog(
            modifier = Modifier.heightIn(min = 200.dp, max = 400.dp),
            onDismissRequest = { showCurrencyDialog.value = false },
            title = { Text(stringResource(R.string.update_currency)) },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        currencyCodes.forEachIndexed { index, currency ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (currency.equals(
                                            newSelection, ignoreCase = true
                                        )), onClick = {
                                            newSelection = currency
                                        })
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (currency.equals(newSelection, ignoreCase = true)),
                                    onClick = null // row handles click
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${currencyDescriptions[index]} (${currency.uppercase()})",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 0.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showCurrencyDialog.value = false
                    onCurrencySelected(newSelection)
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
            })
    }

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.settings))
                }, colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ), scrollBehavior = topAppBarScrollBehavior
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .verticalScroll(scrollState)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val colors =
                ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surface)
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                SectionHeader(stringResource(R.string.data_header), true)
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 0, count = 3
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_currency_exchange_24),
                            stringResource(R.string.update_currency_icon_description)
                        )
                    },
                    content = {
                        Text(stringResource(R.string.update_currency), fontWeight = FontWeight.Bold)
                    },
                    supportingContent = {
                        val index = currencyCodes.indexOf(selectedCurrency)
                        if (index != -1) {
                            Text(
                                "${currencyDescriptions[index]} (${currencyCodes[index].uppercase()})",
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    },
                    onClick = {
                        // show options dialog
                        newSelection = selectedCurrency // reset to current value for UI
                        showCurrencyDialog.value = true
                    },
                )
                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    SegmentedListItem(
                        colors = colors,
                        shapes = ListItemDefaults.segmentedShapes(
                            index = 1, count = 3
                        ),
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.rounded_percent_24),
                                stringResource(R.string.change_percentage_icon_description)
                            )
                        },
                        content = {
                            Text(
                                stringResource(R.string.change_percentage),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        supportingContent = {
                            val changePercentageOptions = arrayOf(
                                stringResource(R.string.cp_interval1),
                                stringResource(R.string.cp_interval2),
                                stringResource(R.string.cp_interval3)
                            )
                            ButtonGroup(
                                overflowIndicator = { },
                                modifier = Modifier.padding(top = 8.dp),
                                expandedRatio = 0.02F,
                                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                                verticalAlignment = Alignment.Top,
                                content = {
                                    changePercentageOptions.forEachIndexed { index, value ->
                                        this.toggleableItem(
                                            checked = changePercentage == index,
                                            label = value,
                                            weight = if (changePercentage == index) 1F else 0.9F,
                                            onCheckedChange = {
                                                onChangePercentageSelected(index)
                                            },
                                        )
                                    }
                                },
                            )
                        },
                        onClick = {}, // this method is intentionally left empty
                    )
                }
                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    SegmentedListItem(
                        colors = colors,
                        shapes = ListItemDefaults.segmentedShapes(
                            index = 2, count = 3
                        ),
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.rounded_timer_24),
                                stringResource(R.string.widget_refresh_interval_icon_description)
                            )
                        },
                        content = {
                            Text(
                                stringResource(R.string.widget_refresh_interval),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        supportingContent = {
                            val intervalOptions = arrayOf(
                                stringResource(R.string.wr_interval1),
                                stringResource(R.string.wr_interval2),
                                stringResource(R.string.wr_interval3)
                            )
                            ButtonGroup(
                                overflowIndicator = { },
                                modifier = Modifier.padding(top = 8.dp),
                                expandedRatio = 0.02F,
                                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                                verticalAlignment = Alignment.Top,
                                content = {
                                    intervalOptions.forEachIndexed { index, value ->
                                        this.toggleableItem(
                                            checked = refreshInterval == index,
                                            label = value,
                                            weight = if (refreshInterval == index) 1F else 0.9F,
                                            onCheckedChange = {
                                                onRefreshIntervalSelected(index)
                                            },
                                        )
                                    }
                                },
                            )
                        },
                        onClick = {}, // this method is intentionally left empty
                    )
                }
                SectionHeader(stringResource(R.string.appearance_header))
                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    SegmentedListItem(
                        colors = colors,
                        shapes = ListItemDefaults.segmentedShapes(
                            index = 0, count = 2
                        ),
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.rounded_brightness_6_24),
                                stringResource(R.string.app_theme_icon_description)
                            )
                        },
                        content = {
                            Text(stringResource(R.string.app_theme), fontWeight = FontWeight.Bold)
                        },
                        supportingContent = {
                            val systemThemeLabel = stringResource(R.string.app_theme_system)
                            val lightThemeLabel = stringResource(R.string.app_theme_light)
                            val darkThemeLabel = stringResource(R.string.app_theme_dark)
                            ButtonGroup(
                                overflowIndicator = { },
                                modifier = Modifier.padding(top = 8.dp),
                                expandedRatio = 0.02F,
                                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                                verticalAlignment = Alignment.Top,
                                content = {
                                    this.toggleableItem(
                                        checked = currentTheme == AppTheme.SYSTEM,
                                        label = systemThemeLabel,
                                        weight = if (currentTheme == AppTheme.SYSTEM) 1F else 0.9F,
                                        onCheckedChange = {
                                            onThemeSelected(AppTheme.SYSTEM)
                                        },
                                    )
                                    this.toggleableItem(
                                        checked = currentTheme == AppTheme.LIGHT,
                                        label = lightThemeLabel,
                                        weight = if (currentTheme == AppTheme.LIGHT) 1F else 0.9F,
                                        onCheckedChange = {
                                            onThemeSelected(AppTheme.LIGHT)
                                        },
                                    )
                                    this.toggleableItem(
                                        checked = currentTheme == AppTheme.DARK,
                                        label = darkThemeLabel,
                                        weight = if (currentTheme == AppTheme.DARK) 1F else 0.9F,
                                        onCheckedChange = {
                                            onThemeSelected(AppTheme.DARK)
                                        },
                                    )
                                },
                            )
                        },
                        onClick = {},
                    )
                }
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 1, count = 2
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_palette_24),
                            stringResource(R.string.dynamic_colors_icon_description)
                        )
                    },
                    enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                    content = {
                        Text(stringResource(R.string.dynamic_colors), fontWeight = FontWeight.Bold)
                    },
                    supportingContent = {
                        Text(
                            stringResource(R.string.dynamic_colors_subtitle),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = dynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                            onCheckedChange = null,
                            enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        )
                    },
                    onClick = {
                        onDynamicColorsSelected(!dynamicColors)
                    },
                )
                SectionHeader(stringResource(R.string.app_info_header))
                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    SegmentedListItem(
                        colors = colors,
                        shapes = ListItemDefaults.segmentedShapes(
                            index = 0, count = if (debugModeEnabled) 4 else 3
                        ),
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.rounded_info_24),
                                stringResource(R.string.info_icon_description)
                            )
                        },
                        content = {
                            Text(stringResource(R.string.version), fontWeight = FontWeight.Bold)
                        },
                        supportingContent = {
                            Text(
                                stringResource(
                                    R.string.version_format,
                                    BuildConfig.VERSION_NAME,
                                    BuildConfig.BUILD_TYPE
                                )
                            )
                        },
                        onClick = {}, // this method is intentionally left empty
                    )
                }
                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    val enabledMessage = stringResource(R.string.debug_enabled)
                    val stepsMessage = stringResource(R.string.developer_steps, 10 - tapCount)
                    SegmentedListItem(
                        colors = colors,
                        shapes = ListItemDefaults.segmentedShapes(
                            index = 1, count = if (debugModeEnabled) 4 else 3
                        ),
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.rounded_build_24),
                                stringResource(R.string.build_icon_description)
                            )
                        },
                        content = {
                            Text(
                                stringResource(R.string.build_number), fontWeight = FontWeight.Bold
                            )
                        },
                        supportingContent = {
                            Text(
                                BuildConfig.VERSION_CODE.toString()
                            )
                        },
                        onClick = {
                            if (!debugModeEnabled) {
                                tapCount++
                                if (tapCount >= 10) {
                                    onDebugModeToggle(true)
                                    Toast.makeText(
                                        context, enabledMessage, Toast.LENGTH_SHORT
                                    ).show()
                                    tapCount = 0
                                } else if (tapCount > 5) {
                                    Toast.makeText(
                                        context, stepsMessage, Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                    )
                }
                SegmentedListItem(
                    colors = colors,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 2, count = if (debugModeEnabled) 4 else 3
                    ),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.rounded_alternate_email_24),
                            stringResource(R.string.contact_icon_description)
                        )
                    },
                    content = {
                        Text(stringResource(R.string.contact), fontWeight = FontWeight.Bold)
                    },
                    supportingContent = {
                        Text(stringResource(R.string.developer))
                    },
                    onClick = {
                        // TODO - open an email client / browser when tapped
                    },
                )
                if (debugModeEnabled) {
                    SegmentedListItem(
                        colors = colors,
                        shapes = ListItemDefaults.segmentedShapes(
                            index = 3, count = 4
                        ),
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.rounded_bug_report_24),
                                stringResource(R.string.debug_icon_description)
                            )
                        },
                        content = {
                            Text(stringResource(R.string.debug_mode), fontWeight = FontWeight.Bold)
                        },
                        supportingContent = {
                            Text(stringResource(R.string.debug_mode_subtitle))
                        },
                        trailingContent = {
                            Switch(
                                checked = debugModeEnabled, onCheckedChange = null
                            )
                        },
                        onClick = {
                            onDebugModeToggle(false)
                        },
                    )
                }
                // to prevent list items from hiding under the floating horizontal bar
                Spacer(
                    Modifier.height(
                        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 100.dp
                    )
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, top: Boolean = false) {
    Text(
        title,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .padding(top = if (top) 4.dp else 16.dp, bottom = 8.dp)
    )
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun SettingsPreview() {
    SettingsScreen(
        selectedCurrency = "usd",
        currentTheme = AppTheme.LIGHT,
        dynamicColors = true,
        refreshInterval = 1
    )
}
